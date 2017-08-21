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

package com.arialyy.simple.download;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import butterknife.Bind;
import com.arialyy.frame.permission.OnPermissionCallback;
import com.arialyy.frame.permission.PermissionManager;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityDownloadMeanBinding;
import com.arialyy.simple.download.fragment_download.FragmentActivity;
import com.arialyy.simple.download.multi_download.MultiTaskActivity;
import com.arialyy.simple.download.service_download.DownloadService;

/**
 * Created by Lyy on 2016/10/13.
 */
public class DownloadActivity extends BaseActivity<ActivityDownloadMeanBinding> {
  @Bind(R.id.single_task) Button mSigleBt;
  @Bind(R.id.multi_task) Button mMultiBt;
  @Bind(R.id.dialog_task) Button mDialogBt;
  @Bind(R.id.pop_task) Button mPopBt;

  @Override protected int setLayoutId() {
    return R.layout.activity_download_mean;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setTitle("Aria下载");
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      setEnable(true);
    } else {  //6.0处理
      boolean hasPermission = PermissionManager.getInstance()
          .checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
      if (hasPermission) {
        setEnable(true);
      } else {
        setEnable(false);
        PermissionManager.getInstance().requestPermission(this, new OnPermissionCallback() {
          @Override public void onSuccess(String... permissions) {
            setEnable(true);
          }

          @Override public void onFail(String... permissions) {
            T.showShort(DownloadActivity.this, "没有文件读写权限");
            setEnable(false);
          }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
      }
    }
  }

  private void setEnable(boolean enable) {
    mSigleBt.setEnabled(enable);
    mMultiBt.setEnabled(enable);
    mDialogBt.setEnabled(enable);
    mPopBt.setEnabled(enable);
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.highest_priority:
        startActivity(new Intent(this, HighestPriorityActivity.class));
        break;
      case R.id.service:
        startService(new Intent(this, DownloadService.class));
        break;
      case R.id.single_task:
        startActivity(new Intent(this, SingleTaskActivity.class));
        break;
      case R.id.multi_task:
        startActivity(new Intent(this, MultiTaskActivity.class));
        break;
      case R.id.dialog_task:
        DownloadDialog dialog = new DownloadDialog(this);
        dialog.show();
        break;
      case R.id.pop_task:
        DownloadPopupWindow pop = new DownloadPopupWindow(this);
        pop.showAtLocation(mRootView, Gravity.CENTER_VERTICAL, 0, 0);
        break;
      case R.id.fragment_task:
        startActivity(new Intent(this, FragmentActivity.class));
        break;
      case R.id.notification:
        SimpleNotification notification = new SimpleNotification(this);
        notification.start();
        break;
    }
  }
}