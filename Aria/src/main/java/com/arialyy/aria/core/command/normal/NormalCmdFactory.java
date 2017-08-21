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

import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.command.AbsCmdFactory;
import com.arialyy.aria.core.inf.AbsTaskEntity;

/**
 * Created by Lyy on 2016/9/23.
 * 命令工厂
 */
public class NormalCmdFactory extends AbsCmdFactory<AbsNormalCmd> {
  /**
   * 创建任务
   */
  public static final int TASK_CREATE = 0x122;
  /**
   * 启动任务
   */
  public static final int TASK_START = 0x123;
  /**
   * 恢复任务
   */
  public static final int TASK_RESUME = 0x127;
  /**
   * 取消任务
   */
  public static final int TASK_CANCEL = 0x124;
  /**
   * 停止任务
   */
  public static final int TASK_STOP = 0x125;
  /**
   * 设置任务为最高优先级
   */
  public static final int TASK_HIGHEST_PRIORITY = 0x128;
  /**
   * 停止所有任务
   */
  public static final int TASK_STOP_ALL = 0x129;
  /**
   * 恢复所有停止的任务
   */
  public static final int TASK_RESUME_ALL = 0x130;
  /**
   * 删除所有任务，
   */
  public static final int TASK_CANCEL_ALL = 0x131;
  private static volatile NormalCmdFactory INSTANCE = null;

  private NormalCmdFactory() {

  }

  public static NormalCmdFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (AriaManager.LOCK) {
        INSTANCE = new NormalCmdFactory();
      }
    }
    return INSTANCE;
  }

  /**
   * @param target 创建任务的对象
   * @param entity 下载实体
   * @param type 命令类型{@link #TASK_CREATE}、{@link #TASK_START}、{@link #TASK_CANCEL}、{@link
   * #TASK_STOP}、{@link #TASK_HIGHEST_PRIORITY}、{@link #TASK_STOP_ALL}、{@link #TASK_RESUME_ALL}
   */
  public <T extends AbsTaskEntity> AbsNormalCmd<T> createCmd(String target, T entity, int type) {
    switch (type) {
      case TASK_CREATE:
        return new AddCmd<>(target, entity);
      case TASK_RESUME:
      case TASK_START:
        return new StartCmd<>(target, entity);
      case TASK_CANCEL:
        return new CancelCmd<>(target, entity);
      case TASK_STOP:
        return new StopCmd<>(target, entity);
      case TASK_HIGHEST_PRIORITY:
        return new HighestPriorityCmd<>(target, entity);
      case TASK_STOP_ALL:
        return new StopAllCmd<>(target, entity);
      case TASK_RESUME_ALL:
        return new ResumeAllCmd<>(target, entity);
      case TASK_CANCEL_ALL:
        return new CancelAllCmd<>(target, entity);
      default:
        return null;
    }
  }
}