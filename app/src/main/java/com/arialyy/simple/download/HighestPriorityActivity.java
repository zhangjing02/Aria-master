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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTarget;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.frame.util.show.L;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityHighestPriorityBinding;
import com.arialyy.simple.download.multi_download.DownloadAdapter;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lyy on 2017/6/2.
 * 最高优先级任务Demo
 */
public class HighestPriorityActivity extends BaseActivity<ActivityHighestPriorityBinding> {
  @Bind(R.id.progressBar) HorizontalProgressBarWithNumber mPb;
  @Bind(R.id.start) Button mStart;
  @Bind(R.id.stop) Button mStop;
  @Bind(R.id.cancel) Button mCancel;
  @Bind(R.id.size) TextView mSize;
  @Bind(R.id.speed) TextView mSpeed;
  @Bind(R.id.list) RecyclerView mList;

  private String mTaskName = "光明大陆";
  private static final String DOWNLOAD_URL =
      "https://res5.d.cn/6f78ee3bcfdd033e64892a8553a95814cf5b4a62b12a76d9eb2a694905f0dc30fa5c7f728806a4ee0b3479e7b26a38707dac92b136add91191ac1219aadb4a3aa70bfa6d06d2d8db.apk";
  private DownloadAdapter mAdapter;
  private List<AbsEntity> mData = new ArrayList<>();
  private Set<String> mRecord = new HashSet<>();

  @Override protected int setLayoutId() {
    return R.layout.activity_highest_priority;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setTitle("最高优先级任务");
    getBinding().setTaskName("任务名：" + mTaskName + " （最高优先级任务）");
    initWidget();
    Aria.download(this).register();
  }

  private void initWidget() {
    DownloadTarget target = Aria.download(this).load(DOWNLOAD_URL);
    mPb.setProgress(target.getPercent());
    if (target.getTaskState() == IEntity.STATE_STOP) {
      mStart.setText("恢复");
      mStart.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
      setBtState(true);
    } else if (target.isDownloading()) {
      setBtState(false);
    }
    mSize.setText(target.getConvertFileSize());
    List<DownloadEntity> temp = Aria.download(this).getSimpleTaskList();
    if (temp != null && !temp.isEmpty()) {
      for (DownloadEntity entity : temp) {
        if (entity.getUrl().equals(DOWNLOAD_URL)) continue;
        mData.add(entity);
        mRecord.add(entity.getUrl());
      }
    }
    mAdapter = new DownloadAdapter(this, mData);
    mList.setLayoutManager(new LinearLayoutManager(this));
    mList.setAdapter(mAdapter);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_highest_priority, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.add_task:
        List<DownloadEntity> temp = getModule(DownloadModule.class).getHighestTestList();
        for (DownloadEntity entity : temp) {
          String url = entity.getUrl();
          if (mRecord.contains(url)) {
            continue;
          }
          mAdapter.addDownloadEntity(entity);
          mRecord.add(url);
        }
        mAdapter.notifyDataSetChanged();
        break;
      case R.id.help:
        String title = "最高优先级任务介绍";
        String msg = " 将任务设置为最高优先级任务，最高优先级任务有以下特点：\n"
            + " 1、在下载队列中，有且只有一个最高优先级任务\n"
            + " 2、最高优先级任务会一直存在，直到用户手动暂停或任务完成\n"
            + " 3、任务调度器不会暂停最高优先级任务\n"
            + " 4、用户手动暂停或任务完成后，第二次重新执行该任务，该命令将失效\n"
            + " 5、如果下载队列中已经满了，则会停止队尾的任务，当高优先级任务完成后，该队尾任务将自动执行\n"
            + " 6、把任务设置为最高优先级任务后，将自动执行任务，不需要重新调用start()启动任务";
        showMsgDialog(title, msg);
        break;
    }
    return true;
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        String text = ((TextView) view).getText().toString();
        if (text.equals("重新开始？") || text.equals("开始")) {
          Aria.download(this)
              .load(DOWNLOAD_URL)
              .setDownloadPath(Environment.getExternalStorageDirectory().getPath()
                  + "/Download/"
                  + mTaskName
                  + ".apk")
              .setHighestPriority();
        } else if (text.equals("恢复")) {
          Aria.download(this).load(DOWNLOAD_URL).resume();
        }
        break;
      case R.id.stop:
        Aria.download(this).load(DOWNLOAD_URL).pause();
        break;
      case R.id.cancel:
        Aria.download(this).load(DOWNLOAD_URL).cancel();
        break;
    }
  }

  /**
   * 设置start 和 stop 按钮状态
   */
  private void setBtState(boolean state) {
    mStart.setEnabled(state);
    mStop.setEnabled(!state);
  }

  @Download.onPre public void onPre(DownloadTask task) {
    mAdapter.updateState(task.getDownloadEntity());
  }

  @Download.onTaskPre public void onTaskPre(DownloadTask task) {
    if (task.getKey().equals(DOWNLOAD_URL)) {
      mSize.setText(task.getConvertFileSize());
    }
    mAdapter.updateState(task.getDownloadEntity());
  }

  @Download.onTaskStart public void onTaskStart(DownloadTask task) {
    if (task.getKey().equals(DOWNLOAD_URL)) {
      setBtState(false);
    }
    mAdapter.updateState(task.getDownloadEntity());
  }

  @Download.onTaskResume public void onTaskResume(DownloadTask task) {
    if (task.getKey().equals(DOWNLOAD_URL)) {
      setBtState(false);
    }
    mAdapter.updateState(task.getDownloadEntity());
  }

  @Download.onTaskStop public void onTaskStop(DownloadTask task) {
    if (task.getKey().equals(DOWNLOAD_URL)) {
      setBtState(true);
      mStart.setText("恢复");
      mStart.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
    }
    mAdapter.updateState(task.getDownloadEntity());
  }

  @Download.onTaskCancel public void onTaskCancel(DownloadTask task) {
    if (task.getKey().equals(DOWNLOAD_URL)) {
      setBtState(true);
      mStart.setText("开始");
      mPb.setProgress(0);
    }
    mAdapter.updateState(task.getDownloadEntity());
  }

  @Download.onTaskFail public void onTaskFail(DownloadTask task) {
    if (task.getKey().equals(DOWNLOAD_URL)) {
      setBtState(true);
    } else {
      L.d(TAG, "download fail【" + task.getKey() + "】");
    }
  }

  @Download.onTaskComplete public void onTaskComplete(DownloadTask task) {
    if (task.getKey().equals(DOWNLOAD_URL)) {
      setBtState(true);
      mStart.setText("重新开始");
      mStart.setTextColor(getResources().getColor(android.R.color.holo_green_light));
      mPb.setProgress(100);
    }
    mAdapter.updateState(task.getDownloadEntity());
  }

  @Download.onTaskRunning public void onTaskRunning(DownloadTask task) {
    if (task.getKey().equals(DOWNLOAD_URL)) {
      mPb.setProgress(task.getPercent());
      mSpeed.setText(task.getConvertSpeed());
    }
    mAdapter.setProgress(task.getDownloadEntity());
  }
}
