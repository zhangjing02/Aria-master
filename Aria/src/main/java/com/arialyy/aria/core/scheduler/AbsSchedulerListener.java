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
 * Created by Aria.Lao on 2017/6/7.
 */
public class AbsSchedulerListener<TASK extends ITask> implements ISchedulerListener<TASK> {
  @Override public void onPre(TASK task) {

  }

  @Override public void onTaskPre(TASK task) {

  }

  @Override public void onTaskResume(TASK task) {

  }

  @Override public void onTaskStart(TASK task) {

  }

  @Override public void onTaskStop(TASK task) {

  }

  @Override public void onTaskCancel(TASK task) {

  }

  @Override public void onTaskFail(TASK task) {

  }

  @Override public void onTaskComplete(TASK task) {

  }

  @Override public void onTaskRunning(TASK task) {

  }

  public void onNoSupportBreakPoint(TASK task) {

  }

  public void setListener(Object obj) {

  }
}