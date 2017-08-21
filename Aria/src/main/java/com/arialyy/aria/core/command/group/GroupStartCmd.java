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
package com.arialyy.aria.core.command.group;

import android.text.TextUtils;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.common.QueueMod;
import com.arialyy.aria.core.inf.AbsGroupTask;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IEntity;

/**
 * Created by AriaL on 2017/6/29.
 * 任务组开始命令，该命令负责开始下载或恢复下载的操作
 */
class GroupStartCmd<T extends AbsTaskEntity> extends AbsGroupCmd<T> {
  /**
   * @param targetName 创建任务的对象名
   */
  GroupStartCmd(String targetName, T entity) {
    super(targetName, entity);
  }

  @Override public void executeCmd() {
    String mod;
    int maxTaskNum;
    AriaManager manager = AriaManager.getInstance(AriaManager.APP);
    if (isDownloadCmd) {
      mod = manager.getDownloadConfig().getQueueMod();
      maxTaskNum = manager.getDownloadConfig().getMaxTaskNum();
    } else {
      mod = manager.getUploadConfig().getQueueMod();
      maxTaskNum = manager.getUploadConfig().getMaxTaskNum();
    }

    AbsGroupTask task = (AbsGroupTask) mQueue.getTask(mTaskEntity.getEntity());
    if (task == null) {
      task = (AbsGroupTask) mQueue.createTask(mTargetName, mTaskEntity);
      if (!TextUtils.isEmpty(mTargetName)) {
        task.setTargetName(mTargetName);
      }
      // 任务不存在时，根据配置不同，对任务执行操作
      if (mod.equals(QueueMod.NOW.getTag())) {
        mQueue.startTask(task);
      } else if (mod.equals(QueueMod.WAIT.getTag())) {
        if (mQueue.getCurrentExePoolNum() < maxTaskNum) {
          mQueue.startTask(task);
        }
      }
    } else {
      // 任务不存在时，根据配置不同，对任务执行操作
      if (!task.isRunning()
          && mod.equals(QueueMod.WAIT.getTag())
          && task.getState() == IEntity.STATE_WAIT) {
        mQueue.startTask(task);
      }
    }
  }
}
