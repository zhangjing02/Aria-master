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
package com.arialyy.aria.core.queue.pool;

import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.inf.AbsTask;
import com.arialyy.aria.util.CommonUtil;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by AriaL on 2017/6/29.
 * 单个下载任务的执行池
 */
class DownloadExecutePool<TASK extends AbsTask> extends BaseExecutePool<TASK> {
  private final String TAG = "DownloadExecutePool";

  @Override protected int getMaxSize() {
    return AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getMaxTaskNum();
  }

  @Override public boolean putTask(TASK task) {
    synchronized (AriaManager.LOCK) {
      if (task == null) {
        Log.e(TAG, "任务不能为空！！");
        return false;
      }
      String url = task.getKey();
      if (mExecuteQueue.contains(task)) {
        if (!task.isRunning()) return true;
        Log.e(TAG, "队列中已经包含了该任务，任务key【" + url + "】");
        return false;
      } else {
        if (mExecuteQueue.size() >= mSize) {
          Set<String> keys = mExecuteMap.keySet();
          for (String key : keys) {
            if (mExecuteMap.get(key).isHighestPriorityTask()) return false;
          }
          if (pollFirstTask()) {
            return putNewTask(task);
          }
        } else {
          return putNewTask(task);
        }
      }
    }
    return false;
  }

  @Override boolean pollFirstTask() {
    try {
      TASK oldTask = mExecuteQueue.poll(TIME_OUT, TimeUnit.MICROSECONDS);
      if (oldTask == null) {
        Log.e(TAG, "移除任务失败");
        return false;
      }
      if (oldTask.isHighestPriorityTask()) {
        return false;
      }
      oldTask.stop();
      String key = CommonUtil.keyToHashKey(oldTask.getKey());
      mExecuteMap.remove(key);
    } catch (InterruptedException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
}
