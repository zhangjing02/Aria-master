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

import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTarget;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivitySingleBinding;

import butterknife.Bind;

public class SingleTaskActivity extends BaseActivity<ActivitySingleBinding> {

  private static final String DOWNLOAD_URL =
      //"http://kotlinlang.org/docs/kotlin-docs.pdf";
      //"https://atom-installer.github.com/v1.13.0/AtomSetup.exe?s=1484074138&ext=.exe";
      //"http://static.gaoshouyou.com/d/22/94/822260b849944492caadd2983f9bb624.apk";
      "http://static.gaoshouyou.com/d/22/94/822260b849944492caadd2983f9bb624.apk";
  //"http://down2.xiaoshuofuwuqi.com/d/file/filetxt/20170608/14/%BA%DA%CE%D7%CA%A6%E1%C8%C6%F0.txt";
  //"http://tinghuaapp.oss-cn-shanghai.aliyuncs.com/20170612201739607815";
  //"http://static.gaoshouyou.com/d/36/69/2d3699acfa69e9632262442c46516ad8.apk";
  //"http://oqcpqqvuf.bkt.clouddn.com/ceshi.txt";
  //"http://down8.androidgame-store.com/201706122321/97967927DD4E53D9905ECAA7874C8128/new/game1/19/45319/com.neuralprisma-2.5.2.174-2000174_1494784835.apk?f=web_1";
  //不支持断点的链接
  //"http://ox.konsung.net:5555/ksdc-web/download/downloadFile/?fileName=ksdc_1.0.2.apk&rRange=0-";
  //"http://172.18.104.50:8080/download/_302turn";
  private static final String DOWNLOAD_URL2 = "http://api.senssun.com/v1/user/downloadApp/10003/2";
  @Bind(R.id.start) Button mStart;
  @Bind(R.id.stop) Button mStop;
  @Bind(R.id.cancel) Button mCancel;
  @Bind(R.id.speeds) RadioGroup mRg;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Aria.download(this).register();
  }

  /**
   * 设置start 和 stop 按钮状态
   */
  private void setBtState(boolean state) {
    mStart.setEnabled(state);
    mStop.setEnabled(!state);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_single_task_activity, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    double speed = -1;
    String msg = "";
    switch (item.getItemId()) {
      case R.id.help:
        msg = "一些小知识点：\n"
            + "1、你可以在注解中增加链接，用于指定被注解的方法只能被特定的下载任务回调，以防止progress乱跳\n"
            + "2、当遇到网络慢的情况时，你可以先使用onPre()更新UI界面，待连接成功时，再在onTaskPre()获取完整的task数据，然后给UI界面设置正确的数据\n"
            + "3、你可以在界面初始化时通过Aria.download(this).load(URL).getPercent()等方法快速获取相关任务的一些数据";
        showMsgDialog("tip", msg);
        break;
      case R.id.speed_0:
        speed = 0.0;
        break;
      case R.id.speed_128:
        speed = 128.0;
        break;
      case R.id.speed_256:
        speed = 256.0;
        break;
      case R.id.speed_512:
        speed = 512.0;
        break;
      case R.id.speed_1m:
        speed = 1024.0;
        break;
    }
    if (speed > -1) {
      msg = item.getTitle().toString();
      Aria.download(this).setMaxSpeed(speed);
      T.showShort(this, msg);
    }
    return true;
  }

  @Download.onPre(DOWNLOAD_URL) protected void onPre(DownloadTask task) {
    setBtState(false);
  }

  @Download.onTaskStart(DOWNLOAD_URL) void taskStart(DownloadTask task) {
    getBinding().setFileSize(task.getConvertFileSize());
  }

  @Download.onTaskRunning(DOWNLOAD_URL) protected void running(DownloadTask task) {
    long len = task.getFileSize();
    if (len == 0) {
      getBinding().setProgress(0);
    } else {
      getBinding().setProgress(task.getPercent());
    }
    getBinding().setSpeed(task.getConvertSpeed());
  }

  @Download.onTaskResume(DOWNLOAD_URL) void taskResume(DownloadTask task) {
    mStart.setText("暂停");
    setBtState(false);
  }

  @Download.onTaskStop(DOWNLOAD_URL) void taskStop(DownloadTask task) {
    mStart.setText("恢复");
    setBtState(true);
    getBinding().setSpeed("");
  }

  @Download.onTaskCancel(DOWNLOAD_URL) void taskCancel(DownloadTask task) {
    getBinding().setProgress(0);
    Toast.makeText(SingleTaskActivity.this, "取消下载", Toast.LENGTH_SHORT).show();
    mStart.setText("开始");
    setBtState(true);
    getBinding().setSpeed("");
  }

  @Download.onTaskFail(DOWNLOAD_URL) void taskFail(DownloadTask task) {
    Toast.makeText(SingleTaskActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
    setBtState(true);
  }

  @Download.onTaskComplete(DOWNLOAD_URL) void taskComplete(DownloadTask task) {
    getBinding().setProgress(100);
    Toast.makeText(SingleTaskActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
    mStart.setText("重新开始？");
    mCancel.setEnabled(false);
    setBtState(true);
    getBinding().setSpeed("");
  }

  @Download.onNoSupportBreakPoint(DOWNLOAD_URL)
  public void onNoSupportBreakPoint(DownloadTask task) {
    T.showShort(SingleTaskActivity.this, "该下载链接不支持断点");
  }

  @Override protected int setLayoutId() {
    return R.layout.activity_single;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setTitle("单任务下载");
    DownloadTarget target = Aria.download(this).load(DOWNLOAD_URL);
    getBinding().setProgress(target.getPercent());
    if (target.getTaskState() == IEntity.STATE_STOP) {
      mStart.setText("恢复");
      mStart.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
      setBtState(true);
    } else if (target.isDownloading()) {
      setBtState(false);
    }
    getBinding().setFileSize(target.getConvertFileSize());
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        Aria.download(this)
            .load(DOWNLOAD_URL)
            .addHeader("groupName", "value")
            .setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/gggg.apk")
            .start();
        break;
      case R.id.stop:
        Aria.download(this).load(DOWNLOAD_URL).stop();
        break;
      case R.id.cancel:
        Aria.download(this).load(DOWNLOAD_URL).cancel();
        break;
    }
  }
}