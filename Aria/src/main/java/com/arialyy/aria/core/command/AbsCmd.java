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

package com.arialyy.aria.core.command;

import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.queue.ITaskQueue;

/**
 * Created by AriaL on 2017/6/29.
 */
public abstract class AbsCmd<T extends AbsTaskEntity> implements ICmd{
  protected ITaskQueue mQueue;
  protected T mTaskEntity;
  protected String TAG;
  protected String mTargetName;
  /**
   * 是否是下载任务的命令
   * {@code true} 下载任务的命令，{@code false} 上传任务的命令
   */
  protected boolean isDownloadCmd = true;
}
