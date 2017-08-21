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
package com.arialyy.aria.core.download.downloader;

import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.common.IUtil;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadGroupTaskEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.IDownloadListener;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by AriaL on 2017/6/30.
 * 任务组核心逻辑
 */
abstract class AbsGroupUtil implements IUtil {
  private final String TAG = "DownloadGroupUtil";
  /**
   * 任务组所有任务总大小
   */
  long mTotalSize = 0;
  protected long mCurrentLocation = 0;
  private ExecutorService mExePool;
  protected IDownloadGroupListener mListener;
  protected DownloadGroupTaskEntity mTaskEntity;
  private boolean isRunning = true;
  private Timer mTimer;
  /**
   * 初始化完成的任务书数
   */
  int mInitNum = 0;
  /**
   * 初始化失败的任务数
   */
  int mInitFailNum = 0;
  /**
   * 保存所有没有下载完成的任务，key为下载地址
   */
  Map<String, DownloadTaskEntity> mExeMap = new HashMap<>();

  /**
   * 下载失败的映射表，key为下载地址
   */
  Map<String, DownloadTaskEntity> mFailMap = new HashMap<>();

  /**
   * 下载器映射表，key为下载地址
   */
  private Map<String, Downloader> mDownloaderMap = new HashMap<>();

  /**
   * 该任务组对应的所有任务
   */
  private Map<String, DownloadTaskEntity> mTasksMap = new HashMap<>();
  //已经完成的任务数
  private int mCompleteNum = 0;
  //失败的任务数
  private int mFailNum = 0;
  //实际的下载任务数
  int mActualTaskNum = 0;

  AbsGroupUtil(IDownloadGroupListener listener, DownloadGroupTaskEntity taskEntity) {
    mListener = listener;
    mTaskEntity = taskEntity;
    mExePool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    List<DownloadTaskEntity> tasks =
        DbEntity.findDatas(DownloadTaskEntity.class, "groupName=?", mTaskEntity.key);
    if (tasks != null && !tasks.isEmpty()) {
      for (DownloadTaskEntity te : tasks) {
        mTasksMap.put(te.getEntity().getUrl(), te);
      }
    }
    for (DownloadEntity entity : mTaskEntity.entity.getSubTask()) {
      File file = new File(entity.getDownloadPath());
      if (entity.getState() == IEntity.STATE_COMPLETE && file.exists()) {
        mCompleteNum++;
        mInitNum++;
        mCurrentLocation += entity.getFileSize();
      } else {
        mExeMap.put(entity.getUrl(), createChildDownloadTask(entity));
        mCurrentLocation += entity.getCurrentProgress();
        mActualTaskNum++;
      }
      mTotalSize += entity.getFileSize();
    }
  }

  @Override public long getFileSize() {
    return mTotalSize;
  }

  @Override public long getCurrentLocation() {
    return mCurrentLocation;
  }

  @Override public boolean isRunning() {
    return isRunning;
  }

  @Override public void cancel() {
    closeTimer(false);
    mListener.onCancel();
    onCancel();
    if (!mExePool.isShutdown()) {
      mExePool.shutdown();
    }

    Set<String> keys = mDownloaderMap.keySet();
    for (String key : keys) {
      Downloader dt = mDownloaderMap.get(key);
      if (dt != null) {
        dt.cancel();
      }
    }
    delDownloadInfo();
    mTaskEntity.deleteData();
  }

  public void onCancel() {

  }

  /**
   * 删除所有子任务的下载信息
   */
  private void delDownloadInfo() {
    List<DownloadTaskEntity> tasks =
        DbEntity.findDatas(DownloadTaskEntity.class, "groupName=?", mTaskEntity.key);
    if (tasks == null || tasks.isEmpty()) return;
    for (DownloadTaskEntity taskEntity : tasks) {
      CommonUtil.delDownloadTaskConfig(taskEntity.removeFile, taskEntity);
    }
  }

  @Override public void stop() {
    closeTimer(false);
    mListener.onStop(mCurrentLocation);
    onStop();
    if (!mExePool.isShutdown()) {
      mExePool.shutdown();
    }

    Set<String> keys = mDownloaderMap.keySet();
    for (String key : keys) {
      Downloader dt = mDownloaderMap.get(key);
      if (dt != null) {
        dt.stop();
      }
    }
  }

  protected void onStop() {

  }

  @Override public void start() {
    isRunning = true;
    mFailNum = 0;
    mListener.onPre();
    onStart();
  }

  protected void onStart() {

  }

  @Override public void resume() {
    start();
    mListener.onResume(mCurrentLocation);
  }

  @Override public void setMaxSpeed(double maxSpeed) {

  }

  private void closeTimer(boolean isRunning) {
    this.isRunning = isRunning;
    if (mTimer != null) {
      mTimer.purge();
      mTimer.cancel();
    }
  }

  /**
   * 开始进度流程
   */
  void startRunningFlow() {
    closeTimer(true);
    mListener.onPostPre(mTotalSize);
    mListener.onStart(mCurrentLocation);
    mTimer = new Timer(true);
    mTimer.schedule(new TimerTask() {
      @Override public void run() {
        if (!isRunning) {
          closeTimer(false);
        } else if (mCurrentLocation >= 0) {
          mListener.onProgress(mCurrentLocation);
        }
      }
    }, 0, 1000);
  }

  /**
   * 启动子任务下载器
   */
  void startChildDownload(DownloadTaskEntity taskEntity) {
    ChildDownloadListener listener = new ChildDownloadListener(taskEntity);
    Downloader dt = new Downloader(listener, taskEntity);
    mDownloaderMap.put(taskEntity.getEntity().getUrl(), dt);
    if (mExePool.isShutdown()) return;
    mExePool.execute(dt);
  }

  /**
   * 创建子任务下载信息
   */
  DownloadTaskEntity createChildDownloadTask(DownloadEntity entity) {
    DownloadTaskEntity taskEntity = mTasksMap.get(entity.getUrl());
    if (taskEntity != null) {
      taskEntity.entity = entity;
      //ftp登录的
      taskEntity.userName = mTaskEntity.userName;
      taskEntity.userPw = mTaskEntity.userPw;
      taskEntity.account = mTaskEntity.account;
      return taskEntity;
    }
    taskEntity = new DownloadTaskEntity();
    taskEntity.entity = entity;
    taskEntity.headers = mTaskEntity.headers;
    taskEntity.requestEnum = mTaskEntity.requestEnum;
    taskEntity.redirectUrlKey = mTaskEntity.redirectUrlKey;
    taskEntity.removeFile = mTaskEntity.removeFile;
    taskEntity.groupName = mTaskEntity.key;
    taskEntity.isGroupTask = true;
    taskEntity.requestType = mTaskEntity.requestType;
    //ftp登录的
    taskEntity.userName = mTaskEntity.userName;
    taskEntity.userPw = mTaskEntity.userPw;
    taskEntity.account = mTaskEntity.account;
    taskEntity.key = entity.getDownloadPath();
    taskEntity.save();
    return taskEntity;
  }

  /**
   * 子任务事件监听
   */
  private class ChildDownloadListener implements IDownloadListener {

    DownloadTaskEntity taskEntity;
    DownloadEntity entity;

    long lastLen = 0;

    ChildDownloadListener(DownloadTaskEntity entity) {
      this.taskEntity = entity;
      this.entity = taskEntity.getEntity();
      lastLen = this.entity.getCurrentProgress();
      this.entity.setFailNum(0);
    }

    @Override public void onPre() {
      saveData(IEntity.STATE_PRE, -1);
    }

    @Override public void onPostPre(long fileSize) {
      entity.setFileSize(fileSize);
      entity.setConvertFileSize(CommonUtil.formatFileSize(fileSize));
      saveData(IEntity.STATE_POST_PRE, -1);
    }

    @Override public void onResume(long resumeLocation) {
      saveData(IEntity.STATE_POST_PRE, IEntity.STATE_RUNNING);
      lastLen = resumeLocation;
    }

    @Override public void onStart(long startLocation) {
      saveData(IEntity.STATE_POST_PRE, IEntity.STATE_RUNNING);
      lastLen = startLocation;
    }

    @Override public void onProgress(long currentLocation) {
      long speed = currentLocation - lastLen;
      mCurrentLocation += speed;
      lastLen = currentLocation;
      entity.setCurrentProgress(currentLocation);
      handleSpeed(speed);
    }

    @Override public void onStop(long stopLocation) {
      saveData(IEntity.STATE_STOP, stopLocation);
      handleSpeed(0);
      mListener.onSubStop(entity);
    }

    @Override public void onCancel() {
      saveData(IEntity.STATE_CANCEL, -1);
      handleSpeed(0);
      mListener.onSubCancel(entity);
    }

    @Override public void onComplete() {
      saveData(IEntity.STATE_COMPLETE, entity.getFileSize());
      mCompleteNum++;
      handleSpeed(0);
      mListener.onSubComplete(entity);
      //如果子任务完成的数量和总任务数一致，表示任务组任务已经完成
      if (mCompleteNum >= mTaskEntity.getEntity().getSubTask().size()) {
        closeTimer(false);
        mListener.onComplete();
      } else if (mCompleteNum + mFailNum >= mActualTaskNum) {
        //如果子任务完成数量加上失败的数量和总任务数一致，则任务组停止下载
        closeTimer(false);
      }
    }

    @Override public void onFail() {
      entity.setFailNum(entity.getFailNum() + 1);
      saveData(IEntity.STATE_FAIL, lastLen);
      handleSpeed(0);
      reTry();
    }

    /**
     * 失败后重试下载，如果失败次数超过5次，不再重试
     */
    private void reTry() {
      synchronized (AriaManager.LOCK) {
        if (entity.getFailNum() < 5 && isRunning) {
          reStartTask();
        } else {
          mFailNum++;
          mListener.onSubFail(entity);
          //如果失败的任务数大于实际的下载任务数，任务组停止下载
          if (mFailNum >= mActualTaskNum) {
            closeTimer(false);
            mListener.onStop(mCurrentLocation);
          }
        }
      }
    }

    private void reStartTask() {
      Timer timer = new Timer();
      timer.schedule(new TimerTask() {
        @Override public void run() {
          Downloader dt = mDownloaderMap.get(entity.getUrl());
          dt.start();
        }
      }, 3000);
    }

    private void handleSpeed(long speed) {
      entity.setSpeed(speed);
      entity.setConvertSpeed(speed <= 0 ? "" : CommonUtil.formatFileSize(speed) + "/s");
    }

    private void saveData(int state, long location) {
      entity.setState(state);
      entity.setComplete(state == IEntity.STATE_COMPLETE);
      if (entity.isComplete()) {
        entity.setCompleteTime(System.currentTimeMillis());
        entity.setCurrentProgress(entity.getFileSize());
      } else if (location > 0) {
        entity.setCurrentProgress(location);
      }
      entity.update();
    }

    @Override public void supportBreakpoint(boolean support) {

    }
  }
}
