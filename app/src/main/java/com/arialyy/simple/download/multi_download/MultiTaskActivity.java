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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import butterknife.Bind;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityMultiBinding;
import com.arialyy.simple.download.DownloadModule;
import com.arialyy.simple.download.SingleTaskActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lyy on 2016/9/27.
 */
public class MultiTaskActivity extends BaseActivity<ActivityMultiBinding> {
  @Bind(R.id.list) RecyclerView mList;
  @Bind(R.id.toolbar) Toolbar mBar;
  private FileListAdapter mAdapter;
  List<FileListEntity> mData = new ArrayList<>();

  @Override protected int setLayoutId() {
    return R.layout.activity_multi;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.download(this).register();
    setTitle("多任务下载");
    mData.addAll(getModule(DownloadModule.class).createMultiTestList());
    mAdapter = new FileListAdapter(this, mData);
    mList.setLayoutManager(new LinearLayoutManager(this));
    mList.setAdapter(mAdapter);
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.num:
        DownloadNumDialog dialog = new DownloadNumDialog(this);
        dialog.show(getSupportFragmentManager(), "download_num");
        break;
      case R.id.stop_all:
        Aria.download(this).stopAllTask();
        break;
      case R.id.turn:
        startActivity(new Intent(this, MultiDownloadActivity.class));
        break;
    }
  }

  @Download.onTaskStart void taskStart(DownloadTask task) {
    mAdapter.updateBtState(task.getKey(), false);
  }

  @Download.onTaskResume void taskResume(DownloadTask task) {
    mAdapter.updateBtState(task.getKey(), false);
  }

  @Download.onTaskStop void taskStop(DownloadTask task) {
    mAdapter.updateBtState(task.getKey(), true);
  }

  @Download.onTaskCancel void taskCancel(DownloadTask task) {
    mAdapter.updateBtState(task.getKey(), true);
  }

  @Download.onTaskFail void taskFail(DownloadTask task) {
    mAdapter.updateBtState(task.getKey(), true);
  }

  @Download.onTaskComplete void taskComplete(DownloadTask task) {
    mAdapter.updateBtState(task.getKey(), true);
  }

  @Override protected void dataCallback(int result, Object data) {
    super.dataCallback(result, data);
    if (result == DownloadNumDialog.RESULT_CODE) {
      Aria.get(this).getDownloadConfig().setMaxTaskNum(Integer.parseInt(data + ""));
    }
  }
}