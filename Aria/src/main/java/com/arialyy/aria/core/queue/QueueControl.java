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
package com.arialyy.aria.core.queue;

import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.core.inf.AbsTask;
import com.arialyy.aria.core.inf.AbsTaskEntity;

/**
 * Created by Aria.Lao on 2017/8/3.
 * 队列控制器，用于处理各种命令
 */
public class QueueControl implements Handler.Callback {
  /**
   * 获取任务命令
   */
  public static final int CMD_GET_TASK = 0xa1;
  /**
   * 创建任务命令
   */
  public static final int CMD_CREATE_TASK = 0xa2;
  /**
   * 启动任务命令
   */
  public static final int CMD_START_TASK = 0xa3;
  /**
   * 停止任务命令
   */
  public static final int CMD_STOP_TASK = 0xa4;
  /**
   * 删除任务命令
   */
  public static final int CMD_CANCEL_TASK = 0xa5;
  /**
   * 停止所有任务命令
   */
  public static final int CMD_STOP_ALL_TASK = 0xa6;
  /**
   * 删除所有任务命令
   */
  public static final int CMD_CANCEL_ALL_TASK = 0xa7;

  /**
   * 队列类型为单文件下载队列
   */
  public static final int TYPE_SIMPLE_DOWNLOAD_QUEUE = 0xc1;
  /**
   * 队列类型为任务组下载队列
   */
  public static final int TYPE_SIMPLE_DOWNLOAD_GROUP_QUEUE = 0xc2;
  /**
   * 队列类型为单文件上传队列
   */
  public static final int TYPE_SIMPLE_UPLOAD_QUEUE = 0xc3;

  private Handler outHandler;
  private AbsTaskQueue queue;

  public QueueControl(Handler.Callback callback, int type) {
    outHandler = new Handler(callback);
    switch (type) {
      case TYPE_SIMPLE_DOWNLOAD_QUEUE:
        queue = DownloadTaskQueue.getInstance();
        break;
      case TYPE_SIMPLE_DOWNLOAD_GROUP_QUEUE:
        queue = DownloadGroupTaskQueue.getInstance();
        break;
      case TYPE_SIMPLE_UPLOAD_QUEUE:
        queue = UploadTaskQueue.getInstance();
        break;
    }
  }

  @Override public boolean handleMessage(Message msg) {
    switch (msg.what) {
      case CMD_GET_TASK:
        outHandler.obtainMessage(CMD_GET_TASK, queue.getTask((AbsEntity) msg.obj)).sendToTarget();
        break;
      case CMD_CREATE_TASK:
        SparseArray params = (SparseArray) msg.obj;
        outHandler.obtainMessage(CMD_CREATE_TASK,
            queue.createTask(String.valueOf(params.get(1)), (AbsTaskEntity) params.get(2)))
            .sendToTarget();
        break;
      case CMD_START_TASK:
        queue.startTask((AbsTask) msg.obj);
        break;
      case CMD_STOP_TASK:
        queue.stopTask((AbsTask) msg.obj);
        break;
      case CMD_CANCEL_TASK:
        queue.removeTask((AbsTask) msg.obj);
        break;
      case CMD_STOP_ALL_TASK:
        queue.stopAllTask();
        break;
      case CMD_CANCEL_ALL_TASK:
        queue.removeAllTask();
        break;
    }
    return true;
  }
}
