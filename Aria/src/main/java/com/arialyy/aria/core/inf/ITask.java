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

/**
 * Created by lyy on 2017/2/13.
 */
public interface ITask<ENTITY extends AbsEntity> {

  /**
   * 获取下载状态
   */
  int getState();

  /**
   * 获取扩展字段
   */
  String getExtendField();

  /**
   * 唯一标识符，DownloadTask 为下载地址，UploadTask 为文件路径
   */
  String getKey();

  /**
   * 任务是否正在执行
   *
   * @return true，正在执行；
   */
  boolean isRunning();

  /**
   * 获取信息实体
   */
  ENTITY getEntity();

  void start();

  void stop();

  void cancel();

  /**
   * 原始byte速度
   */
  long getSpeed();

  /**
   * 转换单位后的速度
   */
  String getConvertSpeed();

  /**
   * 获取百分比进度
   */
  int getPercent();

  /**
   * 原始文件byte长度
   */
  long getFileSize();

  /**
   * 转换单位后的文件长度
   */
  String getConvertFileSize();

  /**
   * 获取当前进度
   */
  long getCurrentProgress();

  /**
   * 获取单位转换后的进度
   *
   * @return 返回 3mb
   */
  String getConvertCurrentProgress();

  void setTargetName(String targetName);

}
