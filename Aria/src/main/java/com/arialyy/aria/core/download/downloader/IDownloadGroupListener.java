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
package com.arialyy.aria.core.download.downloader;

import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.inf.IDownloadListener;
import com.arialyy.aria.core.inf.IEventListener;

/**
 * Created by Aria.Lao on 2017/7/20.
 * 下载任务组事件
 */
public interface IDownloadGroupListener extends IDownloadListener {

  /**
   * 子任务支持断点回调
   *
   * @param support true,支持；false 不支持
   */
  void supportBreakpoint(boolean support, DownloadEntity subEntity);

  /**
   * 子任务开始下载\恢复下载
   */
  void onSubStart(DownloadEntity subEntity);

  /**
   * 子任务停止下载
   */
  void onSubStop(DownloadEntity subEntity);

  /**
   * 子任务下载完成
   */
  void onSubComplete(DownloadEntity subEntity);

  /**
   * 子任务下载失败
   */
  void onSubFail(DownloadEntity subEntity);

  /**
   * 子任务取消下载
   */
  void onSubCancel(DownloadEntity subEntity);
}
