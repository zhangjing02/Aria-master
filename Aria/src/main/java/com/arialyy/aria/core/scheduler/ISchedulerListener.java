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
package com.arialyy.aria.core.scheduler;

import com.arialyy.aria.core.inf.ITask;

/**
 * Target处理任务监听
 */
public interface ISchedulerListener<TASK extends ITask> {
  /**
   * 预处理，有时有些地址链接比较慢，这时可以先在这个地方出来一些界面上的UI，如按钮的状态。
   * 在这个回调中，任务是获取不到文件大小，下载速度等参数
   */
  void onPre(TASK task);

  /**
   * 任务预加载完成
   */
  void onTaskPre(TASK task);

  /**
   * 任务恢复下载
   */
  void onTaskResume(TASK task);

  /**
   * 任务开始
   */
  void onTaskStart(TASK task);

  /**
   * 任务停止
   */
  void onTaskStop(TASK task);

  /**
   * 任务取消
   */
  void onTaskCancel(TASK task);

  /**
   * 任务下载失败
   */
  void onTaskFail(TASK task);

  /**
   * 任务完成
   */
  void onTaskComplete(TASK task);

  /**
   * 任务执行中
   */
  void onTaskRunning(TASK task);
}