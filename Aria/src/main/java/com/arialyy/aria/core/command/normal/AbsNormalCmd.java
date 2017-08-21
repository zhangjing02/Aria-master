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

import com.arialyy.aria.core.command.AbsCmd;
import com.arialyy.aria.core.download.DownloadGroupTaskEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.core.inf.AbsTask;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.queue.DownloadGroupTaskQueue;
import com.arialyy.aria.core.queue.DownloadTaskQueue;
import com.arialyy.aria.core.queue.UploadTaskQueue;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by lyy on 2016/8/22.
 * 下载命令
 */
public abstract class AbsNormalCmd<T extends AbsTaskEntity> extends AbsCmd<T> {
  /**
   * 能否执行命令
   */
  boolean canExeCmd = true;

  private AbsTask tempTask = null;

  /**
   * @param targetName 产生任务的对象名
   */
  AbsNormalCmd(String targetName, T entity) {
    //canExeCmd = CheckUtil.checkCmdEntity(entity,
    //    !(this instanceof CancelCmd) || !(this instanceof StopCmd));
    mTargetName = targetName;
    mTaskEntity = entity;
    TAG = CommonUtil.getClassName(this);
    if (entity instanceof DownloadTaskEntity) {
      mQueue = DownloadTaskQueue.getInstance();
      isDownloadCmd = true;
    } else if (entity instanceof UploadTaskEntity) {
      mQueue = UploadTaskQueue.getInstance();
      isDownloadCmd = false;
    } else if (entity instanceof DownloadGroupTaskEntity) {
      mQueue = DownloadGroupTaskQueue.getInstance();
      isDownloadCmd = true;
    }
  }

  /**
   * 删除所有任务
   */
  void removeAll() {
    mQueue.removeAllTask();
  }

  /**
   * 停止所有任务
   */
  void stopAll() {
    mQueue.stopAllTask();
  }

  /**
   * 停止任务
   */
  void stopTask() {
    if (tempTask == null) createTask();
    mQueue.stopTask(tempTask);
  }

  /**
   * 删除任务
   */
  void removeTask() {
    if (tempTask == null) createTask();
    mQueue.removeTask(tempTask);
  }

  /**
   * 启动任务
   */
  void startTask() {
    mQueue.startTask(tempTask);
  }

  /**
   * 启动指定任务
   *
   * @param task 指定任务
   */
  void startTask(AbsTask task) {
    mQueue.startTask(task);
  }

  /**
   * 从队列中获取任务
   *
   * @return 执行任务
   */
  AbsTask getTask() {
    tempTask = mQueue.getTask(mTaskEntity.getEntity());
    return tempTask;
  }

  /**
   * 从队列中获取任务
   *
   * @return 执行任务
   */
  AbsTask getTask(AbsEntity entity) {
    tempTask = mQueue.getTask(entity);
    return tempTask;
  }

  /**
   * 创建任务
   *
   * @return 创建的任务
   */
  AbsTask createTask() {
    tempTask = mQueue.createTask(mTargetName, mTaskEntity);
    return tempTask;
  }

  /**
   * 创建指定实体的任务
   *
   * @param taskEntity 特定的任务实体
   * @return 创建的任务
   */
  AbsTask createTask(AbsTaskEntity taskEntity) {
    return mQueue.createTask(mTargetName, taskEntity);
  }
}