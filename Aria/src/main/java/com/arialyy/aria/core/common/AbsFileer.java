/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.core.common;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.AbsNormalEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IDownloadListener;
import com.arialyy.aria.core.inf.IEventListener;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by AriaL on 2017/7/1.
 * 文件下载器
 */
public abstract class AbsFileer<ENTITY extends AbsNormalEntity, TASK_ENTITY extends AbsTaskEntity<ENTITY>>
    implements Runnable, IUtil {
  private final String TAG = "Downloader";
  protected IEventListener mListener;
  protected TASK_ENTITY mTaskEntity;
  protected ENTITY mEntity;
  protected File mConfigFile;//信息配置文件
  protected Context mContext;
  protected File mTempFile; //下载的文件
  protected boolean isNewTask = true;
  protected StateConstance mConstance;
  private ExecutorService mFixedThreadPool;
  private int mThreadNum, mRealThreadNum;
  private SparseArray<AbsThreadTask> mTask = new SparseArray<>();

  /**
   * 小于1m的文件不启用多线程
   */
  private static final long SUB_LEN = 1024 * 1024;
  private Timer mTimer;

  protected AbsFileer(IEventListener listener, TASK_ENTITY taskEntity) {
    mListener = listener;
    mTaskEntity = taskEntity;
    mEntity = mTaskEntity.getEntity();
    mContext = AriaManager.APP;
    mConstance = new StateConstance();
  }

  @Override public void setMaxSpeed(double maxSpeed) {
    for (int i = 0; i < mThreadNum; i++) {
      AbsThreadTask task = mTask.get(i);
      if (task != null) {
        task.setMaxSpeed(maxSpeed);
      }
    }
  }

  public StateConstance getConstance() {
    return mConstance;
  }

  @Override public void run() {
    startFlow();
  }

  /**
   * 开始下载流程
   */
  private void startFlow() {
    checkTask();
    if (mListener instanceof IDownloadListener) {
      ((IDownloadListener) mListener).onPostPre(mEntity.getFileSize());
    }
    mConstance.cleanState();
    mConstance.isRunning = true;
    if (!mTaskEntity.isSupportBP) {
      mThreadNum = 1;
      mConstance.THREAD_NUM = mThreadNum;
      handleNoSupportBP();
    } else {
      mThreadNum = isNewTask ? (getNewTaskThreadNum()) : mRealThreadNum;
      mConstance.THREAD_NUM = mThreadNum;
      handleBreakpoint();
    }
    startTimer();
  }

  /**
   * 设置新任务的最大线程数
   */
  protected int getNewTaskThreadNum() {
    return mEntity.getFileSize() <= SUB_LEN || mTaskEntity.requestType == AbsTaskEntity.FTP_DIR ? 1
        : AriaManager.getInstance(mContext).getDownloadConfig().getThreadNum();
  }

  /**
   * 启动进度获取定时器
   */
  private void startTimer() {
    mTimer = new Timer(true);
    mTimer.schedule(new TimerTask() {
      @Override public void run() {
        if (mConstance.isComplete() || !mConstance.isRunning) {
          closeTimer();
        } else if (mConstance.CURRENT_LOCATION >= 0) {
          mListener.onProgress(mConstance.CURRENT_LOCATION);
        }
      }
    }, 0, 1000);
  }

  private void closeTimer() {
    if (mTimer != null) {
      mTimer.purge();
      mTimer.cancel();
    }
  }

  @Override public long getFileSize() {
    return mEntity.getFileSize();
  }

  /**
   * 获取当前下载位置
   */
  @Override public long getCurrentLocation() {
    return mConstance.CURRENT_LOCATION;
  }

  @Override public boolean isRunning() {
    return mConstance.isRunning;
  }

  @Override public void cancel() {
    closeTimer();
    mConstance.isRunning = false;
    mConstance.isCancel = true;
    if (mFixedThreadPool != null) {
      mFixedThreadPool.shutdown();
    }
    for (int i = 0; i < mThreadNum; i++) {
      AbsThreadTask task = mTask.get(i);
      if (task != null) {
        task.cancel();
      }
    }
    if (mTaskEntity instanceof DownloadTaskEntity) {
      CommonUtil.delDownloadTaskConfig(mTaskEntity.removeFile, (DownloadTaskEntity) mTaskEntity);
    } else if (mTaskEntity instanceof UploadTaskEntity) {
      CommonUtil.delUploadTaskConfig(mTaskEntity.removeFile, (UploadTaskEntity) mTaskEntity);
    }
  }

  @Override public void stop() {
    closeTimer();
    if (mConstance.isComplete()) return;
    mConstance.isStop = true;
    mConstance.isRunning = false;
    if (mFixedThreadPool != null) {
      mFixedThreadPool.shutdown();
    }
    for (int i = 0; i < mThreadNum; i++) {
      AbsThreadTask task = mTask.get(i);
      if (task != null) {
        task.stop();
      }
    }
  }

  /**
   * 直接调用的时候会自动启动线程执行
   */
  @Override public void start() {
    new Thread(this).start();
  }

  @Override public void resume() {
    start();
  }

  /**
   * 返回该下载器的
   */
  public IEventListener getListener() {
    return mListener;
  }

  /**
   * 检查任务是否是新任务，新任务条件：
   * 1、文件不存在
   * 2、下载记录文件不存在
   * 3、下载记录文件缺失或不匹配
   * 4、数据库记录不存在
   * 5、不支持断点，则是新任务
   */
  protected abstract void checkTask();

  /**
   * 检查记录文件，如果是新任务返回{@code true}，否则返回{@code false}
   */
  protected boolean checkConfigFile() {
    Properties pro = CommonUtil.loadConfig(mConfigFile);
    if (pro.isEmpty()) {
      return true;
    }
    Set<Object> keys = pro.keySet();
    int num = 0;
    for (Object key : keys) {
      if (String.valueOf(key).contains("_record_")) {
        num++;
      }
    }
    if (num == 0) {
      return true;
    }
    mRealThreadNum = num;
    for (int i = 0; i < mRealThreadNum; i++) {
      if (pro.getProperty(mTempFile.getName() + "_record_" + i) == null) {
        Object state = pro.getProperty(mTempFile.getName() + "_state_" + i);
        if (state != null && Integer.parseInt(state + "") == 1) {
          continue;
        }
        return true;
      }
    }
    return false;
  }

  /**
   * 恢复记录地址
   *
   * @return true 表示下载完成
   */
  private boolean resumeRecordLocation(int i, long startL, long endL) {
    mConstance.CURRENT_LOCATION += endL - startL;
    Log.d(TAG, "++++++++++ 线程_" + i + "_已经下载完成 ++++++++++");
    mConstance.COMPLETE_THREAD_NUM++;
    mConstance.STOP_NUM++;
    mConstance.CANCEL_NUM++;
    if (mConstance.isComplete()) {
      if (mConfigFile.exists()) {
        mConfigFile.delete();
      }
      mListener.onComplete();
      mConstance.isRunning = false;
      return true;
    }
    return false;
  }

  /**
   * 启动断点任务时，创建单线程任务
   *
   * @param i 线程id
   * @param startL 该任务起始位置
   * @param endL 该任务结束位置
   * @param fileLength 该任务需要处理的文件长度
   */
  private AbsThreadTask createSingThreadTask(int i, long startL, long endL, long fileLength) {
    SubThreadConfig<TASK_ENTITY> config = new SubThreadConfig<>();
    config.FILE_SIZE = fileLength;
    config.URL = mEntity.isRedirect() ? mEntity.getRedirectUrl() : mEntity.getUrl();
    config.TEMP_FILE = mTempFile;
    config.THREAD_ID = i;
    config.START_LOCATION = startL;
    config.END_LOCATION = endL;
    config.CONFIG_FILE_PATH = mConfigFile.getPath();
    config.SUPPORT_BP = mTaskEntity.isSupportBP;
    config.TASK_ENTITY = mTaskEntity;
    return selectThreadTask(config);
  }

  /**
   * 处理断点
   */
  private void handleBreakpoint() {
    long fileLength = mEntity.getFileSize();
    Properties pro = CommonUtil.loadConfig(mConfigFile);
    long blockSize = fileLength / mThreadNum;
    int[] recordL = new int[mThreadNum];
    for (int i = 0; i < mThreadNum; i++) {
      recordL[i] = -1;
    }
    int rl = 0;
    if (isNewTask) {
      handleNewTask();
    }
    for (int i = 0; i < mThreadNum; i++) {
      long startL = i * blockSize, endL = (i + 1) * blockSize;
      Object state = pro.getProperty(mTempFile.getName() + "_state_" + i);
      if (state != null && Integer.parseInt(state + "") == 1) {  //该线程已经完成
        if (resumeRecordLocation(i, startL, endL)) return;
        continue;
      }
      //分配下载位置
      Object record = pro.getProperty(mTempFile.getName() + "_record_" + i);
      //如果有记录，则恢复下载
      if (!isNewTask && record != null && Long.parseLong(record + "") >= 0) {
        Long r = Long.parseLong(record + "");
        mConstance.CURRENT_LOCATION += r - startL;
        Log.d(TAG, "任务【" + mEntity.getFileName() + "】线程__" + i + "__恢复下载");
        startL = r;
        recordL[rl] = i;
        rl++;
      } else {
        recordL[rl] = i;
        rl++;
      }
      if (i == (mThreadNum - 1)) {
        //最后一个线程的结束位置即为文件的总长度
        endL = fileLength;
      }
      AbsThreadTask task = createSingThreadTask(i, startL, endL, fileLength);
      if (task == null) return;
      mTask.put(i, task);
    }
    startSingleTask(recordL);
  }

  /**
   * 启动单线程下载任务
   */
  private void startSingleTask(int[] recordL) {
    if (mConstance.CURRENT_LOCATION > 0) {
      mListener.onResume(mConstance.CURRENT_LOCATION);
    } else {
      mListener.onStart(mConstance.CURRENT_LOCATION);
    }
    mFixedThreadPool = Executors.newFixedThreadPool(recordL.length);
    for (int l : recordL) {
      if (l == -1) continue;
      Runnable task = mTask.get(l);
      if (task != null) {
        mFixedThreadPool.execute(task);
      }
    }
  }

  /**
   * 处理新任务
   */
  protected abstract void handleNewTask();

  /**
   * 处理不支持断点的下载
   */
  private void handleNoSupportBP() {
    SubThreadConfig<TASK_ENTITY> config = new SubThreadConfig<>();
    config.FILE_SIZE = mEntity.getFileSize();
    config.URL = mEntity.isRedirect() ? mEntity.getRedirectUrl() : mEntity.getUrl();
    config.TEMP_FILE = mTempFile;
    config.THREAD_ID = 0;
    config.START_LOCATION = 0;
    config.END_LOCATION = config.FILE_SIZE;
    config.CONFIG_FILE_PATH = mConfigFile.getPath();
    config.SUPPORT_BP = mTaskEntity.isSupportBP;
    config.TASK_ENTITY = mTaskEntity;
    AbsThreadTask task = selectThreadTask(config);
    if (task == null) return;
    mTask.put(0, task);
    mFixedThreadPool = Executors.newFixedThreadPool(1);
    mFixedThreadPool.execute(task);
    mListener.onStart(0);
  }

  /**
   * 选择单任务线程的类型
   */
  protected abstract AbsThreadTask selectThreadTask(SubThreadConfig<TASK_ENTITY> config);

  protected void failDownload(String errorMsg) {
    closeTimer();
    Log.e(TAG, errorMsg);
    mConstance.isRunning = false;
    mListener.onFail();
  }
}
