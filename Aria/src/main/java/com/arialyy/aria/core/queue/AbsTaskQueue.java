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

import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.core.inf.AbsTask;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.queue.pool.BaseCachePool;
import com.arialyy.aria.core.queue.pool.BaseExecutePool;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by lyy on 2017/2/23.
 * 任务队列
 */
abstract class AbsTaskQueue<TASK extends AbsTask, TASK_ENTITY extends AbsTaskEntity, ENTITY extends AbsEntity>
    implements ITaskQueue<TASK, TASK_ENTITY, ENTITY> {
  private final String TAG = "AbsTaskQueue";
  BaseCachePool<TASK> mCachePool;
  BaseExecutePool<TASK> mExecutePool;

  AbsTaskQueue() {
    mCachePool = setCachePool();
    mExecutePool = setExecutePool();
  }

  abstract BaseCachePool<TASK> setCachePool();

  abstract BaseExecutePool<TASK> setExecutePool();

  @Override public boolean taskIsRunning(String key) {
    return mExecutePool.getTask(key) != null;
  }

  @Override public void removeAllTask() {
    for (String key : mExecutePool.getAllTask().keySet()) {
      TASK task = mExecutePool.getAllTask().get(key);
      if (task != null) task.cancel();
    }
    for (String key : mCachePool.getAllTask().keySet()) {
      mCachePool.removeTask(key);
    }
  }

  /**
   * 停止所有任务
   */
  @Override public void stopAllTask() {
    for (String key : mExecutePool.getAllTask().keySet()) {
      TASK task = mExecutePool.getAllTask().get(key);
      if (task != null && task.isRunning()) task.stop();
    }
    for (String key : mCachePool.getAllTask().keySet()) {
      mCachePool.removeTask(key);
    }
  }

  @Override public int getMaxTaskNum() {
    return AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getMaxTaskNum();
  }

  /**
   * 获取实体的索引
   */
  public abstract String getKey(ENTITY entity);

  /**
   * 获取配置文件配置的最大可执行任务数
   */
  public abstract int getConfigMaxNum();

  /**
   * 获取任务执行池
   */
  public BaseExecutePool getExecutePool() {
    return mExecutePool;
  }

  /**
   * 获取缓存池
   */
  public BaseCachePool getCachePool() {
    return mCachePool;
  }

  /**
   * 获取缓存任务数
   *
   * @return 获取缓存的任务数
   */
  @Override public int getCurrentCachePoolNum() {
    return mCachePool.size();
  }

  /**
   * 获取执行池中的任务数量
   *
   * @return 当前正在执行的任务数
   */
  @Override public int getCurrentExePoolNum() {
    return mExecutePool.size();
  }

  @Override public void setMaxTaskNum(int downloadNum) {
    int oldMaxSize = getConfigMaxNum();
    int diff = downloadNum - oldMaxSize;
    if (oldMaxSize == downloadNum) {
      Log.d(TAG, "设置的下载任务数和配置文件的下载任务数一直，跳过");
      return;
    }
    //设置的任务数小于配置任务数
    if (diff <= -1 && mExecutePool.size() >= oldMaxSize) {
      for (int i = 0, len = Math.abs(diff); i < len; i++) {
        TASK eTask = mExecutePool.pollTask();
        if (eTask != null) {
          stopTask(eTask);
        }
      }
    }
    mExecutePool.setMaxNum(downloadNum);
    if (diff >= 1) {
      for (int i = 0; i < diff; i++) {
        TASK nextTask = getNextTask();
        if (nextTask != null && nextTask.getState() == IEntity.STATE_WAIT) {
          startTask(nextTask);
        }
      }
    }
  }

  @Override public TASK getTask(String url) {
    TASK task = mExecutePool.getTask(url);
    if (task == null) {
      task = mCachePool.getTask(url);
    }
    return task;
  }

  @Override public void startTask(TASK task) {
    if (mExecutePool.putTask(task)) {
      mCachePool.removeTask(task);
      task.getEntity().setFailNum(0);
      task.start();
    }
  }

  @Override public void stopTask(TASK task) {
    if (!task.isRunning()) Log.w(TAG, "停止任务失败，【任务已经停止】");
    if (mExecutePool.removeTask(task)) {
      task.stop();
    } else {
      task.stop();
      Log.w(TAG, "删除任务失败，【执行队列中没有该任务】");
    }
  }

  @Override public void removeTask(ENTITY entity) {
    TASK task = mExecutePool.getTask(getKey(entity));
    if (task != null) {
      Log.d(TAG, "从执行池删除任务，删除" + (mExecutePool.removeTask(task) ? "成功" : "失败"));
    }
    task = mCachePool.getTask(getKey(entity));
    if (task != null) {
      Log.d(TAG, "从缓存池删除任务，删除" + (mCachePool.removeTask(task) ? "成功" : "失败"));
    }
  }

  @Override public void reTryStart(TASK task) {
    if (task == null) {
      Log.w(TAG, "重试下载失败，task 为null");
      return;
    }
    if (!task.isRunning()) {
      task.start();
    } else {
      Log.w(TAG, "任务没有完全停止，重试下载失败");
    }
  }

  @Override public TASK getTask(ENTITY entity) {
    return getTask(getKey(entity));
  }

  @Override public void removeTask(TASK task) {
    task.cancel();
  }

  @Override public TASK getNextTask() {
    return mCachePool.pollTask();
  }
}
