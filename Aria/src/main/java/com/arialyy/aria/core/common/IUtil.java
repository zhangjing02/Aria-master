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

package com.arialyy.aria.core.common;

/**
 * Created by lyy on 2016/10/31.
 * 抽象的下载接口
 */
public interface IUtil {

  /**
   * 获取文件大小
   */
  long getFileSize();

  /**
   * 获取当前下载位置
   */
  long getCurrentLocation();

  /**
   * 是否正在下载
   *
   * @return true, 正在下载
   */
  boolean isRunning();

  /**
   * 取消下载
   */
  void cancel();

  /**
   * 停止下载
   */
  void stop();

  /**
   * 开始下载
   */
  void start();

  /**
   * 从上次断点恢复下载
   */
  void resume();

  /**
   * 设置最大下载速度
   */
  void setMaxSpeed(double maxSpeed);
}