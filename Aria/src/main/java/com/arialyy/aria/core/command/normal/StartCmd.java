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
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.common.QueueMod;
import com.arialyy.aria.core.inf.AbsTask;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;

/**
 * Created by lyy on 2016/8/22.
 * 开始命令
 * 队列模型{@link QueueMod#NOW}、{@link QueueMod#WAIT}
 */
class StartCmd<T extends AbsTaskEntity> extends AbsNormalCmd<T> {

  StartCmd(String targetName, T entity) {
    super(targetName, entity);
  }

  @Override public void executeCmd() {
    if (!canExeCmd) return;
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

    AbsTask task = getTask();
    if (task == null) {
      task = createTask();
      if (!TextUtils.isEmpty(mTargetName)) {
        task.setTargetName(mTargetName);
      }
      // 任务不存在时，根据配置不同，对任务执行操作
      if (mod.equals(QueueMod.NOW.getTag())) {
        startTask();
      } else if (mod.equals(QueueMod.WAIT.getTag())) {
        if (mQueue.getCurrentExePoolNum() < maxTaskNum
            || task.getState() == IEntity.STATE_STOP
            || task.getState() == IEntity.STATE_COMPLETE) {
          startTask();
        }
      }
    } else {
      // 任务不存在时，根据配置不同，对任务执行操作
      if (!task.isRunning()) {
        startTask();
      }
    }
  }
}