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
package com.arialyy.aria.core.command.normal;

import android.text.TextUtils;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.inf.AbsNormalTask;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.queue.DownloadTaskQueue;

/**
 * Created by lyy on 2017/6/2.
 * 最高优先级命令，最高优先级命令有以下属性
 * 1、在下载队列中，有且只有一个最高优先级任务
 * 2、最高优先级任务会一直存在，直到用户手动暂停或任务完成
 * 3、任务调度器不会暂停最高优先级任务
 * 4、用户手动暂停或任务完成后，第二次重新执行该任务，该命令将失效
 * 5、如果下载队列中已经满了，则会停止队尾的任务，当高优先级任务完成后，该队尾任务将自动执行
 * 6、把任务设置为最高优先级任务后，将自动执行任务，不需要重新调用start()启动任务
 *
 * 目前只只支持单下载任务的最高优先级任务
 */
final class HighestPriorityCmd<T extends AbsTaskEntity> extends AbsNormalCmd<T> {
  /**
   * @param targetName 产生任务的对象名
   */
  HighestPriorityCmd(String targetName, T entity) {
    super(targetName, entity);
  }

  @Override public void executeCmd() {
    if (!canExeCmd) return;
    DownloadTask task = (DownloadTask) getTask();
    if (task == null) {
      task = (DownloadTask) createTask();
    }
    if (task != null) {
      if (!TextUtils.isEmpty(mTargetName)) {
        task.setTargetName(mTargetName);
      }
      ((DownloadTaskQueue) mQueue).setTaskHighestPriority(task);
    }
  }
}
