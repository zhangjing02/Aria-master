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
package com.arialyy.aria.core.upload.uploader;

import com.arialyy.aria.core.common.IUtil;
import com.arialyy.aria.core.inf.IUploadListener;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import com.arialyy.aria.util.CheckUtil;

/**
 * Created by lyy on 2017/2/9.
 * 简单的文件上传工具
 */
public class SimpleUploadUtil implements IUtil, Runnable {
  private static final String TAG = "SimpleUploadUtil";

  private UploadEntity mUploadEntity;
  private UploadTaskEntity mTaskEntity;
  private IUploadListener mListener;
  private Uploader mUploader;

  public SimpleUploadUtil(UploadTaskEntity taskEntity, IUploadListener listener) {
    mTaskEntity = taskEntity;
    CheckUtil.checkTaskEntity(taskEntity);
    mUploadEntity = taskEntity.getEntity();
    if (listener == null) {
      throw new IllegalArgumentException("上传监听不能为空");
    }
    mListener = listener;
    mUploader = new Uploader(mListener, taskEntity);
  }

  @Override public void run() {
    mListener.onPre();
    mUploader.start();
  }

  @Override public long getFileSize() {
    return mUploader.getFileSize();
  }

  @Override public long getCurrentLocation() {
    return mUploader.getCurrentLocation();
  }

  @Override public boolean isRunning() {
    return mUploader.isRunning();
  }

  @Override public void cancel() {
    mUploader.cancel();
  }

  @Override public void stop() {
    mUploader.stop();
  }

  @Override public void start() {
    new Thread(this).start();
  }

  @Override public void resume() {
    mUploader.cancel();
  }

  @Override public void setMaxSpeed(double maxSpeed) {
    mUploader.setMaxSpeed(maxSpeed);
  }
}
