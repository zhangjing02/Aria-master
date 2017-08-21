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
package com.arialyy.aria.core.inf;

import java.util.List;

/**
 * Created by lyy on 2017/2/6.
 */
public interface IReceiver<ENTITY extends IEntity> {
  /**
   * Receiver 销毁
   */
  void destroy();

  /**
   * 移除事件回调
   */
  void removeSchedulerListener();

  /**
   * 移除观察者
   */
  void unRegister();

  /**
   * 停止所有任务
   */
  void stopAllTask();

  /**
   * 删除所有任务
   */
  void removeAllTask(boolean removeFile);

  /**
   * 任务是否存在
   *
   * @param key 下载时为下载路径，上传时为文件路径
   */
  boolean taskExists(String key);

  /**
   * 获取任务列表
   */
  List<ENTITY> getSimpleTaskList();
}
