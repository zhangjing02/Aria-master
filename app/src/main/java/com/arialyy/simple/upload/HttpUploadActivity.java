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

package com.arialyy.simple.upload;

import android.os.Bundle;
import butterknife.Bind;
import butterknife.OnClick;
import com.arialyy.annotations.Upload;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.upload.UploadTask;
import com.arialyy.frame.util.show.L;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityUploadBinding;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;

/**
 * Created by Aria.Lao on 2017/2/9.
 */
public class HttpUploadActivity extends BaseActivity<ActivityUploadBinding> {
  private static final String TAG = "HttpUploadActivity";
  @Bind(R.id.pb) HorizontalProgressBarWithNumber mPb;

  private static final String FILE_PATH = "/sdcard/large.rar";

  @Override protected int setLayoutId() {
    return R.layout.activity_upload;
  }

  @Override protected void init(Bundle savedInstanceState) {
    setTile("HTTP 上传");
    super.init(savedInstanceState);
    Aria.upload(this).register();
  }

  @OnClick(R.id.upload) void upload() {
    Aria.upload(this)
        .load(FILE_PATH)
        .setUploadUrl("http://172.18.104.129:8080/upload/sign_file")
        .setAttachment("file")
        .start();
  }

  @OnClick(R.id.stop) void stop() {
    Aria.upload(this).load(FILE_PATH).cancel();
  }

  @OnClick(R.id.remove) void remove() {
    Aria.upload(this).load(FILE_PATH).cancel();
  }

  @Upload.onPre public void onPre(UploadTask task) {
  }

  @Upload.onTaskStart public void taskStart(UploadTask task) {
    L.d(TAG, "upload start");
    getBinding().setFileSize(task.getConvertFileSize());
  }

  @Upload.onTaskStop public void taskStop(UploadTask task) {
    L.d(TAG, "upload stop");
    getBinding().setSpeed("");
    getBinding().setProgress(0);
  }

  @Upload.onTaskCancel public void taskCancel(UploadTask task) {
    L.d(TAG, "upload cancel");
    getBinding().setSpeed("");
    getBinding().setProgress(0);
  }

  @Upload.onTaskRunning public void taskRunning(UploadTask task) {
    getBinding().setSpeed(task.getConvertSpeed());
    getBinding().setProgress(task.getPercent());
  }

  @Upload.onTaskComplete public void taskComplete(UploadTask task) {
    L.d(TAG, "上传完成");
    getBinding().setSpeed("");
    getBinding().setProgress(100);
  }
}
