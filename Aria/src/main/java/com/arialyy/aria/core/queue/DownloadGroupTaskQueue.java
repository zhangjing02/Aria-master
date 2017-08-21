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

package com.arialyy.aria.core.queue;

import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.download.DownloadGroupTask;
import com.arialyy.aria.core.download.DownloadGroupTaskEntity;
import com.arialyy.aria.core.queue.pool.BaseCachePool;
import com.arialyy.aria.core.queue.pool.BaseExecutePool;
import com.arialyy.aria.core.queue.pool.DownloadSharePool;
import com.arialyy.aria.core.scheduler.DownloadGroupSchedulers;

/**
 * Created by AriaL on 2017/6/29.
 * 任务组下载队列
 */
public class DownloadGroupTaskQueue
    extends AbsTaskQueue<DownloadGroupTask, DownloadGroupTaskEntity, DownloadGroupEntity> {
  private static volatile DownloadGroupTaskQueue INSTANCE = null;

  private final String TAG = "DownloadGroupTaskQueue";

  public static DownloadGroupTaskQueue getInstance() {
    if (INSTANCE == null) {
      synchronized (AriaManager.LOCK) {
        INSTANCE = new DownloadGroupTaskQueue();
      }
    }
    return INSTANCE;
  }

  private DownloadGroupTaskQueue() {
  }

  @Override BaseCachePool<DownloadGroupTask> setCachePool() {
    return DownloadSharePool.getInstance().cachePool;
  }

  @Override BaseExecutePool<DownloadGroupTask> setExecutePool() {
    return DownloadSharePool.getInstance().executePool;
  }

  @Override public DownloadGroupTask createTask(String targetName, DownloadGroupTaskEntity entity) {
    DownloadGroupTask task = null;
    if (!TextUtils.isEmpty(targetName)) {
      task = (DownloadGroupTask) TaskFactory.getInstance()
          .createTask(targetName, entity, DownloadGroupSchedulers.getInstance());
      entity.key = entity.getEntity().getGroupName();
      mCachePool.putTask(task);
    } else {
      Log.e(TAG, "target name 为 null！！");
    }
    return task;
  }

  @Override public String getKey(DownloadGroupEntity entity) {
    return entity.getGroupName();
  }

  @Override public int getConfigMaxNum() {
    return AriaManager.getInstance(AriaManager.APP).getDownloadConfig().oldMaxTaskNum;
  }
}
