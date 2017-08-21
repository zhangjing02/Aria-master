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

import com.arialyy.aria.core.inf.AbsTaskEntity;

/**
 * Created by AriaL on 2017/6/29.
 * 停止任务组的命令
 */
class GroupStopCmd<T extends AbsTaskEntity> extends AbsGroupCmd<T>{
  /**
   * @param targetName 创建任务的对象名
   */
  GroupStopCmd(String targetName, T entity) {
    super(targetName, entity);
  }

  @Override public void executeCmd() {

  }
}
