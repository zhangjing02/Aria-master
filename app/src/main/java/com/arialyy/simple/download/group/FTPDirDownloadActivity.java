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
package com.arialyy.simple.download.group;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import butterknife.Bind;
import com.arialyy.annotations.DownloadGroup;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.download.DownloadGroupTask;
import com.arialyy.aria.core.download.DownloadGroupTaskEntity;
import com.arialyy.frame.util.show.L;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityDownloadGroupBinding;
import com.arialyy.simple.widget.SubStateLinearLayout;

/**
 * Created by Aria.Lao on 2017/7/6.
 */
public class FTPDirDownloadActivity extends BaseActivity<ActivityDownloadGroupBinding> {
  private static final String dir = "ftp://172.18.104.129:21/haha/";

  @Bind(R.id.child_list) SubStateLinearLayout mChildList;

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.download(this).register();
    setTitle("FTP文件夹下载");
    DownloadGroupTaskEntity entity = Aria.download(this).getDownloadGroupTask(dir);
    if (entity != null && entity.getEntity() != null) {
      DownloadGroupEntity groupEntity = entity.getEntity();
      mChildList.addData(groupEntity.getSubTask());
      getBinding().setFileSize(groupEntity.getConvertFileSize());
      if (groupEntity.getFileSize() == 0) {
        getBinding().setProgress(0);
      } else {
        getBinding().setProgress(groupEntity.isComplete() ? 100
            : (int) (groupEntity.getCurrentProgress() * 100 / groupEntity.getFileSize()));
      }
    }
  }

  @Override protected int setLayoutId() {
    return R.layout.activity_download_group;
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        Aria.download(this)
            .loadFtpDir(dir)
            .setDownloadDirPath(
                Environment.getExternalStorageDirectory().getPath() + "/Download/ftp_dir")
            .setGroupAlias("ftp文件夹下载")
            //.setSubTaskFileName(getModule(GroupModule.class).getSubName())
            .login("lao", "123456")
            .start();
        break;
      case R.id.stop:
        Aria.download(this).loadFtpDir(dir).stop();
        break;
      case R.id.cancel:
        Aria.download(this).loadFtpDir(dir).cancel();
        break;
    }
  }

  @DownloadGroup.onPre() protected void onPre(DownloadGroupTask task) {
    L.d(TAG, "group pre");
  }

  @DownloadGroup.onTaskPre() protected void onTaskPre(DownloadGroupTask task) {
    if (mChildList.getSubData().size() <= 0) {
      mChildList.addData(task.getEntity().getSubTask());
    }
    L.d(TAG, "group task pre");
    getBinding().setFileSize(task.getConvertFileSize());
  }

  @DownloadGroup.onTaskStart() void taskStart(DownloadGroupTask task) {
    L.d(TAG, "group task start");
  }

  @DownloadGroup.onTaskRunning() protected void running(DownloadGroupTask task) {
    getBinding().setProgress(task.getPercent());
    getBinding().setSpeed(task.getConvertSpeed());
    mChildList.updateChildProgress(task.getEntity().getSubTask());
  }

  @DownloadGroup.onTaskResume() void taskResume(DownloadGroupTask task) {
    L.d(TAG, "group task resume");
  }

  @DownloadGroup.onTaskStop() void taskStop(DownloadGroupTask task) {
    L.d(TAG, "group task stop");
    getBinding().setSpeed("");
  }

  @DownloadGroup.onTaskCancel() void taskCancel(DownloadGroupTask task) {
    getBinding().setSpeed("");
    getBinding().setProgress(0);
  }

  @DownloadGroup.onTaskFail() void taskFail(DownloadGroupTask task) {
    L.d(TAG, "group task fail");
  }

  @DownloadGroup.onTaskComplete() void taskComplete(DownloadGroupTask task) {
    getBinding().setProgress(100);
    mChildList.updateChildProgress(task.getEntity().getSubTask());
    T.showShort(this, "任务组下载完成");
    L.d(TAG, "任务组下载完成");
  }
}
