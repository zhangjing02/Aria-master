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
package com.arialyy.aria.core.upload;

import android.os.Handler;
import android.util.Log;
import com.arialyy.aria.core.inf.AbsNormalTask;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.scheduler.ISchedulers;
import com.arialyy.aria.core.upload.uploader.SimpleUploadUtil;

/**
 * Created by lyy on 2017/2/23.
 * 上传任务
 */
public class UploadTask extends AbsNormalTask<UploadEntity> {
  private static final String TAG = "UploadTask";

  private SimpleUploadUtil mUtil;
  private BaseUListener<UploadEntity, UploadTask> mListener;

  private UploadTask(UploadTaskEntity taskEntity, Handler outHandler) {
    mOutHandler = outHandler;
    mEntity = taskEntity.getEntity();
    mListener = new BaseUListener<>(this, mOutHandler);
    mUtil = new SimpleUploadUtil(taskEntity, mListener);
  }

  @Override public String getKey() {
    return mEntity.getFilePath();
  }

  @Override public boolean isRunning() {
    return mUtil.isRunning();
  }

  @Override public void start() {
    if (mUtil.isRunning()) {
      Log.d(TAG, "任务正在下载");
    } else {
      mUtil.start();
    }
  }

  @Override public void stop() {
    if (mUtil.isRunning()) {
      mUtil.stop();
    } else {
      mEntity.setState(IEntity.STATE_STOP);
      mEntity.update();
      if (mOutHandler != null) {
        mOutHandler.obtainMessage(ISchedulers.STOP, this).sendToTarget();
      }
    }
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
    private Handler mOutHandler;
    private UploadTaskEntity mTaskEntity;
    private String mTargetName;

    public void setOutHandler(ISchedulers outHandler) {
      mOutHandler = new Handler(outHandler);
    }

    public void setUploadTaskEntity(UploadTaskEntity taskEntity) {
      mTaskEntity = taskEntity;
    }

    public void setTargetName(String targetName) {
      mTargetName = targetName;
    }

    public Builder() {

    }

    public UploadTask build() {
      UploadTask task = new UploadTask(mTaskEntity, mOutHandler);
      task.setTargetName(mTargetName);
      return task;
    }
  }
}
