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
package com.arialyy.aria.core.download;

import android.os.Handler;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.downloader.DownloadGroupUtil;
import com.arialyy.aria.core.download.downloader.FtpDirDownloadUtil;
import com.arialyy.aria.core.common.IUtil;
import com.arialyy.aria.core.inf.AbsGroupTask;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.scheduler.ISchedulers;
import com.arialyy.aria.util.CheckUtil;

/**
 * Created by AriaL on 2017/6/27.
 * 任务组任务
 */
public class DownloadGroupTask extends AbsGroupTask<DownloadGroupTaskEntity, DownloadGroupEntity> {
  private final String TAG = "DownloadGroupTask";
  private DownloadGroupListener mListener;
  private IUtil mUtil;

  private DownloadGroupTask(DownloadGroupTaskEntity taskEntity, Handler outHandler) {
    mTaskEntity = taskEntity;
    mEntity = taskEntity.getEntity();
    mOutHandler = outHandler;
    mContext = AriaManager.APP;
    mListener = new DownloadGroupListener(this, mOutHandler);
    switch (taskEntity.requestType) {
      case AbsTaskEntity.HTTP:
        mUtil = new DownloadGroupUtil(mListener, mTaskEntity);
        break;
      case  AbsTaskEntity.FTP_DIR:
        mUtil = new FtpDirDownloadUtil(mListener, mTaskEntity);
        break;
    }
  }

  @Override public boolean isRunning() {
    return mUtil.isRunning();
  }

  @Override public void start() {
    mUtil.start();
  }

  @Override public void stop() {
    if (!mUtil.isRunning()) {
      if (mOutHandler != null) {
        mOutHandler.obtainMessage(ISchedulers.STOP, this).sendToTarget();
      }
    }
    mUtil.stop();
  }

  @Override public void cancel() {
    if (!mUtil.isRunning()) {
      if (mOutHandler != null) {
        mOutHandler.obtainMessage(ISchedulers.CANCEL, this).sendToTarget();
      }
    }
    mUtil.cancel();
  }

  public static class Builder {
    DownloadGroupTaskEntity taskEntity;
    Handler outHandler;
    String targetName;

    public Builder(String targetName, DownloadGroupTaskEntity taskEntity) {
      CheckUtil.checkTaskEntity(taskEntity);
      this.targetName = targetName;
      this.taskEntity = taskEntity;
    }

    /**
     * 设置自定义Handler处理下载状态时间
     *
     * @param schedulers {@link ISchedulers}
     */
    public DownloadGroupTask.Builder setOutHandler(ISchedulers schedulers) {
      this.outHandler = new Handler(schedulers);
      return this;
    }

    public DownloadGroupTask build() {
      DownloadGroupTask task = new DownloadGroupTask(taskEntity, outHandler);
      task.setTargetName(targetName);
      taskEntity.save();
      return task;
    }
  }
}
