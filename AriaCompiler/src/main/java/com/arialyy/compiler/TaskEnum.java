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
package com.arialyy.compiler;

/**
 * Created by Aria.Lao on 2017/7/10.
 * 任务类型枚举
 */
enum TaskEnum {
  DOWNLOAD("com.arialyy.aria.core.download", "DownloadTask",
      "$$DownloadListenerProxy"), DOWNLOAD_GROUP("com.arialyy.aria.core.download",
      "DownloadGroupTask", "$$DownloadGroupListenerProxy"), UPLOAD("com.arialyy.aria.core.upload",
      "UploadTask", "$$UploadListenerProxy"), UPLOAD_GROUP("com.arialyy.aria.core.upload",
      "UploadGroupTask", "$$UploadGroupListenerProxy");

  String pkg, className, proxySuffix;

  public String getClassName() {
    return className;
  }

  public String getProxySuffix() {
    return proxySuffix;
  }

  public String getPkg() {
    return pkg;
  }

  /**
   * @param pkg 包名
   * @param className 任务完整类名
   * @param proxySuffix 事件代理后缀
   */
  TaskEnum(String pkg, String className, String proxySuffix) {
    this.pkg = pkg;
    this.className = className;
    this.proxySuffix = proxySuffix;
  }
}
