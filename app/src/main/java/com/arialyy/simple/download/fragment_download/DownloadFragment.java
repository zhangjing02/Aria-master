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

package com.arialyy.simple.download.fragment_download;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.download.DownloadTarget;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.frame.core.AbsFragment;
import com.arialyy.simple.R;
import com.arialyy.simple.databinding.FragmentDownloadBinding;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;

/**
 * Created by Aria.Lao on 2017/1/4.
 */
public class DownloadFragment extends AbsFragment<FragmentDownloadBinding> {
  @Bind(R.id.start) Button mStart;
  @Bind(R.id.stop) Button mStop;
  @Bind(R.id.cancel) Button mCancel;

  private static final String DOWNLOAD_URL = "https://res5.d.cn/2137e42d610b3488d9420c6421529386eee5bdbfd9be1fafe0a05d6dabaec8c156ddbd00581055bbaeac03904fb63310e80010680235d16bd4c040b50096a0c20dd1c4b0854529a1.apk";

  @Override protected void init(Bundle savedInstanceState) {
    if (Aria.download(this).taskExists(DOWNLOAD_URL)) {
      DownloadTarget target = Aria.download(this).load(DOWNLOAD_URL);
      getBinding().setProgress(target.getPercent());
    }
    DownloadEntity entity = Aria.download(this).getDownloadEntity(DOWNLOAD_URL);
    if (entity != null) {
      getBinding().setFileSize(CommonUtil.formatFileSize(entity.getFileSize()));
      int state = entity.getState();
      setBtState(state != DownloadEntity.STATE_RUNNING);
    } else {
      setBtState(true);
    }
    Aria.download(this).register();
  }

  @OnClick({ R.id.start, R.id.stop, R.id.cancel }) public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        Aria.download(this)
            .load(DOWNLOAD_URL)
            .setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/王者军团.apk")
            .start();
        break;
      case R.id.stop:
        Aria.download(this).load(DOWNLOAD_URL).pause();
        break;
      case R.id.cancel:
        Aria.download(this).load(DOWNLOAD_URL).cancel();
        break;
    }
  }

  @Download.onTaskPre public void onTaskPre(DownloadTask task) {
    getBinding().setFileSize(task.getConvertFileSize());
    setBtState(false);
  }

  @Download.onTaskStop public void onTaskStop(DownloadTask task) {
    setBtState(true);
    getBinding().setSpeed("");
  }

  @Download.onTaskCancel public void onTaskCancel(DownloadTask task) {
    setBtState(true);
    getBinding().setProgress(0);
    getBinding().setSpeed("");
  }

  @Download.onTaskRunning public void onTaskRunning(DownloadTask task) {
    long len = task.getFileSize();
    if (len == 0) {
      getBinding().setProgress(0);
    } else {
      getBinding().setProgress(task.getPercent());
    }
    getBinding().setSpeed(task.getConvertSpeed());
  }

  @Override protected void onDelayLoad() {

  }

  @Override protected int setLayoutId() {
    return R.layout.fragment_download;
  }

  @Override protected void dataCallback(int result, Object obj) {

  }

  private void setBtState(boolean startEnable) {
    mStart.setEnabled(startEnable);
    mCancel.setEnabled(!startEnable);
    mStop.setEnabled(!startEnable);
  }
}
