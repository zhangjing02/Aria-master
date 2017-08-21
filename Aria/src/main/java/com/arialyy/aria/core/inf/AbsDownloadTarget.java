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

import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.command.normal.NormalCmdFactory;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by lyy on 2017/2/28.
 */
public abstract class AbsDownloadTarget<TARGET extends AbsTarget, ENTITY extends AbsEntity, TASK_ENTITY extends AbsTaskEntity>
    extends AbsTarget<TARGET, ENTITY, TASK_ENTITY> {

  /**
   * 如果你的下载链接的header中含有md5码信息，那么你可以通过设置key，来获取从header获取该md5码信息。
   * key默认值为：Content-MD5
   * 获取md5信息：{@link DownloadEntity#getMd5Code()}
   */
  public TARGET setHeaderMd5Key(String md5Key) {
    if (TextUtils.isEmpty(md5Key)) return (TARGET) this;
    mTaskEntity.md5Key = md5Key;
    if (TextUtils.isEmpty(mTaskEntity.md5Key) || !mTaskEntity.md5Key.equals(md5Key)) {
      mTaskEntity.update();
    }
    return (TARGET) this;
  }

  /**
   * 如果你的文件长度是放在header中，那么你需要配置key来让Aria知道正确的文件长度
   * key默认值为：Content-Length
   */
  public TARGET setHeaderContentLengthKey(String contentLength) {
    if (TextUtils.isEmpty(contentLength)) return (TARGET) this;
    mTaskEntity.contentLength = contentLength;
    if (TextUtils.isEmpty(mTaskEntity.contentLength) || !mTaskEntity.contentLength.equals(
        contentLength)) {
      mTaskEntity.update();
    }
    return (TARGET) this;
  }

  /**
   * 如果你的下载链接的header中含有文件描述信息，那么你可以通过设置key，来获取从header获取该文件描述信息。
   * key默认值为：Content-Disposition
   * 获取文件描述信息：{@link DownloadEntity#getDisposition()}
   */
  public TARGET setHeaderDispositionKey(String dispositionKey) {
    if (TextUtils.isEmpty(dispositionKey)) return (TARGET) this;
    mTaskEntity.dispositionKey = dispositionKey;
    if (TextUtils.isEmpty(mTaskEntity.dispositionKey) || !mTaskEntity.dispositionKey.equals(
        dispositionKey)) {
      mTaskEntity.save();
    }
    return (TARGET) this;
  }

  /**
   * 从文件描述信息{@link #setHeaderDispositionKey(String)}中含有文件名信息，你可以通过设置key来获取header中的文件名
   * key默认值为：attachment;filename
   * 获取文件名信息：{@link DownloadEntity#getServerFileName()}
   */
  public TARGET setHeaderDispositionFileKey(String dispositionFileKey) {
    if (TextUtils.isEmpty(dispositionFileKey)) return (TARGET) this;
    mTaskEntity.dispositionFileKey = dispositionFileKey;
    if (TextUtils.isEmpty(mTaskEntity.dispositionFileKey) || !mTaskEntity.dispositionFileKey.equals(
        dispositionFileKey)) {
      mTaskEntity.save();
    }
    return (TARGET) this;
  }

  /**
   * 将任务设置为最高优先级任务，最高优先级任务有以下特点：
   * 1、在下载队列中，有且只有一个最高优先级任务
   * 2、最高优先级任务会一直存在，直到用户手动暂停或任务完成
   * 3、任务调度器不会暂停最高优先级任务
   * 4、用户手动暂停或任务完成后，第二次重新执行该任务，该命令将失效
   * 5、如果下载队列中已经满了，则会停止队尾的任务，当高优先级任务完成后，该队尾任务将自动执行
   * 6、把任务设置为最高优先级任务后，将自动执行任务，不需要重新调用start()启动任务
   */
  protected void setHighestPriority() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(
            CommonUtil.createCmd(mTargetName, mTaskEntity, NormalCmdFactory.TASK_HIGHEST_PRIORITY))
        .exe();
  }

  /**
   * 重定向后，新url的key，默认为location
   */
  public void setRedirectUrlKey(String redirectUrlKey) {
    if (TextUtils.isEmpty(redirectUrlKey)) {
      Log.w("AbsDownloadTarget", "重定向后，新url的key不能为null");
      return;
    }
    mTaskEntity.redirectUrlKey = redirectUrlKey;
  }

  /**
   * 获取任务文件大小
   *
   * @return 文件大小
   */
  public long getFileSize() {
    return getSize();
  }

  /**
   * 获取单位转换后的文件大小
   *
   * @return 文件大小{@code xxx mb}
   */
  public String getConvertFileSize() {
    return getConvertSize();
  }

  /**
   * 添加任务
   */
  public void add() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(mTargetName, mTaskEntity, NormalCmdFactory.TASK_CREATE))
        .exe();
  }

  /**
   * 重新下载
   */
  public void reStart() {
    cancel();
    start();
  }
}
