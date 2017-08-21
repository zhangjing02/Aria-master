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

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IEventListener;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;

/**
 * Created by lyy on 2017/1/18.
 * 下载线程
 */
public abstract class AbsThreadTask<ENTITY extends AbsEntity, TASK_ENTITY extends AbsTaskEntity<ENTITY>>
    implements Runnable {
  private final String TAG = "AbsThreadTask";
  protected long mChildCurrentLocation = 0, mSleepTime = 0;
  protected int mBufSize;
  protected String mConfigFPath;
  protected IEventListener mListener;
  protected StateConstance STATE;
  protected SubThreadConfig<TASK_ENTITY> mConfig;
  protected ENTITY mEntity;
  protected TASK_ENTITY mTaskEntity;
  /**
   * FTP 服务器编码
   */
  public static String SERVER_CHARSET = "ISO-8859-1";

  protected AbsThreadTask(StateConstance constance, IEventListener listener,
      SubThreadConfig<TASK_ENTITY> info) {
    AriaManager manager = AriaManager.getInstance(AriaManager.APP);
    STATE = constance;
    STATE.CONNECT_TIME_OUT = manager.getDownloadConfig().getConnectTimeOut();
    STATE.READ_TIME_OUT = manager.getDownloadConfig().getIOTimeOut();
    mListener = listener;
    mConfig = info;
    mTaskEntity = mConfig.TASK_ENTITY;
    mEntity = mTaskEntity.getEntity();
    if (mConfig.SUPPORT_BP) {
      mConfigFPath = info.CONFIG_FILE_PATH;
    }
    mBufSize = manager.getDownloadConfig().getBuffSize();
    setMaxSpeed(AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getMsxSpeed());
  }

  public void setMaxSpeed(double maxSpeed) {
    if (-0.9999 < maxSpeed && maxSpeed < 0.00001) {
      mSleepTime = 0;
    } else {
      BigDecimal db = new BigDecimal(
          ((mBufSize / 1024) * (filterVersion() ? 1 : STATE.THREAD_NUM) / maxSpeed) * 1000);
      mSleepTime = db.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
    }
  }

  private boolean filterVersion() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
  }

  /**
   * 停止下载
   */
  public void stop() {
    synchronized (AriaManager.LOCK) {
      try {
        if (mConfig.SUPPORT_BP) {
          STATE.STOP_NUM++;
          Log.d(TAG, "任务【"
              + mConfig.TEMP_FILE.getName()
              + "】thread__"
              + mConfig.THREAD_ID
              + "__停止, stop location ==> "
              + mChildCurrentLocation);
          writeConfig(false, mChildCurrentLocation);
          if (STATE.isStop()) {
            Log.d(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】已停止");
            STATE.isRunning = false;
            mListener.onStop(STATE.CURRENT_LOCATION);
          }
        } else {
          Log.d(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】已停止");
          STATE.isRunning = false;
          mListener.onStop(STATE.CURRENT_LOCATION);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 下载中
   */
  protected void progress(long len) {
    synchronized (AriaManager.LOCK) {
      mChildCurrentLocation += len;
      STATE.CURRENT_LOCATION += len;
    }
  }

  /**
   * 取消下载
   */
  public void cancel() {
    synchronized (AriaManager.LOCK) {
      if (mConfig.SUPPORT_BP) {
        STATE.CANCEL_NUM++;
        Log.d(TAG,
            "任务【" + mConfig.TEMP_FILE.getName() + "】thread__" + mConfig.THREAD_ID + "__取消下载");
        if (STATE.isCancel()) {
          File configFile = new File(mConfigFPath);
          if (configFile.exists()) {
            configFile.delete();
          }
          if (mConfig.TEMP_FILE.exists() && !(mEntity instanceof UploadEntity)) {
            mConfig.TEMP_FILE.delete();
          }
          Log.d(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】已取消");
          STATE.isRunning = false;
          mListener.onCancel();
        }
      } else {
        Log.d(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】已取消");
        STATE.isRunning = false;
        mListener.onCancel();
      }
    }
  }

  /**
   * 任务失败
   */
  protected void fail(long currentLocation, String msg, Exception ex) {
    synchronized (AriaManager.LOCK) {
      try {
        STATE.FAIL_NUM++;
        STATE.isRunning = false;
        STATE.isStop = true;
        if (ex != null) {
          Log.e(TAG, msg + "\n" + CommonUtil.getPrintException(ex));
        } else {
          Log.e(TAG, msg);
        }
        if (mConfig.SUPPORT_BP) {
          writeConfig(false, currentLocation);
          if (STATE.isFail()) {
            Log.e(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】执行失败");
            mListener.onFail();
          }
        } else {
          Log.e(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】执行失败");
          mListener.onFail();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 将记录写入到配置文件
   */
  protected void writeConfig(boolean isComplete, long record) throws IOException {
    synchronized (AriaManager.LOCK) {
      String key = null, value = null;
      if (0 < record && record < mConfig.END_LOCATION) {
        key = mConfig.TEMP_FILE.getName() + "_record_" + mConfig.THREAD_ID;
        value = String.valueOf(record);
      } else if (record >= mConfig.END_LOCATION || isComplete) {
        key = mConfig.TEMP_FILE.getName() + "_state_" + mConfig.THREAD_ID;
        value = "1";
      }
      if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
        File configFile = new File(mConfigFPath);
        Properties pro = CommonUtil.loadConfig(configFile);
        pro.setProperty(key, value);
        CommonUtil.saveConfig(configFile, pro);
      }
    }
  }
}
