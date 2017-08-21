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

package com.arialyy.simple.download.multi_download;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import butterknife.Bind;
import com.arialyy.annotations.Download;
import com.arialyy.annotations.DownloadGroup;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadGroupTask;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.frame.util.show.L;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityMultiDownloadBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AriaL on 2017/1/6.
 */
public class MultiDownloadActivity extends BaseActivity<ActivityMultiDownloadBinding> {
  @Bind(R.id.list) RecyclerView mList;
  private DownloadAdapter mAdapter;
  private List<AbsEntity> mData = new ArrayList<>();

  @Override protected int setLayoutId() {
    return R.layout.activity_multi_download;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.download(this).register();
    setTitle("下载列表");
    List<AbsEntity> temps = Aria.download(this).getTotleTaskList();
    if (temps != null && !temps.isEmpty()) {
      mData.addAll(temps);
    }
    mAdapter = new DownloadAdapter(this, mData);
    mList.setLayoutManager(new LinearLayoutManager(this));
    mList.setAdapter(mAdapter);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_mutil_task, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    Aria.download(this).resumeAllTask();
    return true;
  }

  @Download.onPre void onPre(DownloadTask task) {
    mAdapter.updateState(task.getEntity());
  }

  @Download.onTaskStart void taskStart(DownloadTask task) {
    mAdapter.updateState(task.getEntity());
  }

  @Download.onTaskResume void taskResume(DownloadTask task) {
    mAdapter.updateState(task.getEntity());
  }

  @Download.onTaskStop void taskStop(DownloadTask task) {
    mAdapter.updateState(task.getEntity());
  }

  @Download.onTaskCancel void taskCancel(DownloadTask task) {
    mAdapter.updateState(task.getEntity());
  }

  @Download.onTaskFail void taskFail(DownloadTask task) {
    mAdapter.updateState(task.getEntity());
  }

  @Download.onTaskComplete void taskComplete(DownloadTask task) {
    mAdapter.updateState(task.getEntity());
  }

  @Download.onTaskRunning() void taskRunning(DownloadTask task) {
    mAdapter.setProgress(task.getEntity());
  }

  //////////////////////////////////// 下面为任务组的处理 /////////////////////////////////////////

  @DownloadGroup.onPre void onGroupPre(DownloadGroupTask task) {
    mAdapter.updateState(task.getEntity());
  }

  @DownloadGroup.onTaskStart void groupTaskStart(DownloadGroupTask task) {
    mAdapter.updateState(task.getEntity());
  }

  @DownloadGroup.onTaskResume void groupTaskResume(DownloadGroupTask task) {
    mAdapter.updateState(task.getEntity());
  }

  @DownloadGroup.onTaskStop void groupTaskStop(DownloadGroupTask task) {
    mAdapter.updateState(task.getEntity());
  }

  @DownloadGroup.onTaskCancel void groupTaskCancel(DownloadGroupTask task) {
    mAdapter.updateState(task.getEntity());
  }

  @DownloadGroup.onTaskFail void groupTaskFail(DownloadGroupTask task) {
    mAdapter.updateState(task.getEntity());
  }

  @DownloadGroup.onTaskComplete void groupTaskComplete(DownloadGroupTask task) {
    mAdapter.updateState(task.getEntity());
  }

  @DownloadGroup.onTaskRunning() void groupTaskRunning(DownloadGroupTask task) {
    mAdapter.setProgress(task.getEntity());
  }
}
