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

import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.command.AbsCmdFactory;
import com.arialyy.aria.core.inf.AbsTaskEntity;

/**
 * Created by AriaL on 2017/6/29.
 */
class GroupCmdFactory extends AbsCmdFactory<AbsGroupCmd> {
  /**
   * 启动任务
   */
  public static final int TASK_START = 0xa1;
  /**
   * 停止任务
   */
  public static final int TASK_STOP = 0xa2;
  /**
   * 取消任务
   */
  public static final int TASK_CANCEL = 0xa3;

  private static volatile GroupCmdFactory INSTANCE = null;

  private GroupCmdFactory() {

  }

  public static GroupCmdFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (AriaManager.LOCK) {
        INSTANCE = new GroupCmdFactory();
      }
    }
    return INSTANCE;
  }

  /**
   * @param target 创建任务的对象
   * @param entity 下载实体
   * @param type 命令类型{@link #TASK_START}、{@link #TASK_CANCEL}、{@link #TASK_STOP}
   */
  public <T extends AbsTaskEntity> AbsGroupCmd<T> createCmd(String target, T entity, int type) {
    switch (type) {
      case TASK_START:
        return new GroupStartCmd<>(target, entity);
      case TASK_STOP:
        return new GroupStopCmd<>(target, entity);
      case TASK_CANCEL:
        return new GroupCancelCmd<>(target, entity);
      default:
        return null;
    }
  }
}
