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

import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.DownloadGroupTask;
import com.arialyy.aria.core.download.DownloadGroupTaskEntity;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.ITask;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.scheduler.DownloadSchedulers;
import com.arialyy.aria.core.scheduler.ISchedulers;
import com.arialyy.aria.core.scheduler.UploadSchedulers;
import com.arialyy.aria.core.upload.UploadTask;
import com.arialyy.aria.core.upload.UploadTaskEntity;

/**
 * Created by lyy on 2016/8/18.
 * 任务工厂
 */
class TaskFactory {

  private static volatile TaskFactory INSTANCE = null;

  private TaskFactory() {

  }

  public static TaskFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (AriaManager.LOCK) {
        INSTANCE = new TaskFactory();
      }
    }
    return INSTANCE;
  }

  /**
   * 创建任务
   *
   * @param entity 下载实体
   * @param schedulers 对应的任务调度器
   * @param <TASK_ENTITY> {@link DownloadTaskEntity}、{@link UploadTaskEntity}、{@link
   * DownloadGroupTaskEntity}
   * @param <SCHEDULER> {@link DownloadSchedulers}、{@link UploadSchedulers}
   * @return {@link DownloadTask}、{@link UploadTask}、{@link DownloadGroupTask}
   */
  <TASK_ENTITY extends AbsTaskEntity, SCHEDULER extends ISchedulers> ITask createTask(
      String targetName, TASK_ENTITY entity, SCHEDULER schedulers) {
    if (entity instanceof DownloadTaskEntity) {
      return createDownloadTask(targetName, (DownloadTaskEntity) entity, schedulers);
    } else if (entity instanceof UploadTaskEntity) {
      return createUploadTask(targetName, (UploadTaskEntity) entity, schedulers);
    } else if (entity instanceof DownloadGroupTaskEntity) {
      return createDownloadGroupTask(targetName, (DownloadGroupTaskEntity) entity, schedulers);
    }
    return null;
  }

  /**
   * 创建下载任务主任务
   *
   * @param entity 下载任务实体{@link DownloadGroupTask}
   * @param schedulers {@link ISchedulers}
   */
  private DownloadGroupTask createDownloadGroupTask(String targetName,
      DownloadGroupTaskEntity entity, ISchedulers schedulers) {
    DownloadGroupTask.Builder builder = new DownloadGroupTask.Builder(targetName, entity);
    builder.setOutHandler(schedulers);
    return builder.build();
  }

  /**
   * @param entity 上传任务实体{@link UploadTaskEntity}
   * @param schedulers {@link ISchedulers}
   */
  private UploadTask createUploadTask(String targetName, UploadTaskEntity entity,
      ISchedulers schedulers) {
    UploadTask.Builder builder = new UploadTask.Builder();
    builder.setTargetName(targetName);
    builder.setUploadTaskEntity(entity);
    builder.setOutHandler(schedulers);
    return builder.build();
  }

  /**
   * @param entity 下载任务实体{@link DownloadTaskEntity}
   * @param schedulers {@link ISchedulers}
   */
  private DownloadTask createDownloadTask(String targetName, DownloadTaskEntity entity,
      ISchedulers schedulers) {
    DownloadTask.Builder builder = new DownloadTask.Builder(targetName, entity);
    builder.setOutHandler(schedulers);
    return builder.build();
  }
}