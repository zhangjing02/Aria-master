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
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.queue.pool.BaseCachePool;
import com.arialyy.aria.core.queue.pool.BaseExecutePool;
import com.arialyy.aria.core.queue.pool.DownloadSharePool;
import com.arialyy.aria.core.scheduler.DownloadSchedulers;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by lyy on 2016/8/17.
 * 下载任务队列
 */
public class DownloadTaskQueue
    extends AbsTaskQueue<DownloadTask, DownloadTaskEntity, DownloadEntity> {
  private static final String TAG = "DownloadTaskQueue";
  private static volatile DownloadTaskQueue INSTANCE = null;

  public static DownloadTaskQueue getInstance() {
    if (INSTANCE == null) {
      synchronized (AriaManager.LOCK) {
        INSTANCE = new DownloadTaskQueue();
      }
    }
    return INSTANCE;
  }

  private DownloadTaskQueue() {
  }

  @Override BaseCachePool<DownloadTask> setCachePool() {
    return DownloadSharePool.getInstance().cachePool;
  }

  @Override BaseExecutePool<DownloadTask> setExecutePool() {
    return DownloadSharePool.getInstance().executePool;
  }

  @Override public String getKey(DownloadEntity entity) {
    return entity.getUrl();
  }

  @Override public int getConfigMaxNum() {
    return AriaManager.getInstance(AriaManager.APP).getDownloadConfig().oldMaxTaskNum;
  }

  /**
   * 设置任务为最高优先级任务
   */
  public void setTaskHighestPriority(DownloadTask task) {
    task.setHighestPriority(true);
    Map<String, DownloadTask> exeTasks = mExecutePool.getAllTask();
    if (exeTasks != null && !exeTasks.isEmpty()) {
      Set<String> keys = exeTasks.keySet();
      for (String key : keys) {
        DownloadTask temp = exeTasks.get(key);
        if (temp != null && temp.isRunning() && temp.isHighestPriorityTask() && !temp.getKey()
            .equals(task.getKey())) {
          Log.e(TAG, "设置最高优先级任务失败，失败原因【任务中已经有最高优先级任务，请等待上一个最高优先级任务完成，或手动暂停该任务】");
          task.setHighestPriority(false);
          return;
        }
      }
    }
    int maxSize = AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getMaxTaskNum();
    int currentSize = mExecutePool.size();
    if (currentSize == 0 || currentSize < maxSize) {
      startTask(task);
    } else {
      Set<DownloadTask> tempTasks = new LinkedHashSet<>();
      for (int i = 0; i < maxSize; i++) {
        DownloadTask oldTsk = mExecutePool.pollTask();
        if (oldTsk != null && oldTsk.isRunning()) {
          if (i == maxSize - 1) {
            oldTsk.stopAndWait();
            mCachePool.putTaskToFirst(oldTsk);
            break;
          }
          tempTasks.add(oldTsk);
        }
      }
      startTask(task);

      for (DownloadTask temp : tempTasks) {
        mExecutePool.putTask(temp);
      }
    }
  }

  /**
   * 最大下载速度
   */
  public void setMaxSpeed(double maxSpeed) {
    Map<String, DownloadTask> tasks = mExecutePool.getAllTask();
    Set<String> keys = tasks.keySet();
    for (String key : keys) {
      DownloadTask task = tasks.get(key);
      task.setMaxSpeed(maxSpeed);
    }
  }

  @Override public DownloadTask createTask(String target, DownloadTaskEntity entity) {
    DownloadTask task = null;
    if (!TextUtils.isEmpty(target)) {
      task = (DownloadTask) TaskFactory.getInstance()
          .createTask(target, entity, DownloadSchedulers.getInstance());
      entity.key = entity.getEntity().getDownloadPath();
      mCachePool.putTask(task);
    } else {
      Log.e(TAG, "target name 为 null！！");
    }

    return task;
  }

  @Override public void stopTask(DownloadTask task) {
    task.setHighestPriority(false);
    super.stopTask(task);
  }
}