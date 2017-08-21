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
package com.arialyy.aria.core.upload;

import android.support.annotation.NonNull;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.common.ProxyHelper;
import com.arialyy.aria.core.command.normal.NormalCmdFactory;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.AbsReceiver;
import com.arialyy.aria.core.inf.IReceiver;
import com.arialyy.aria.core.scheduler.ISchedulerListener;
import com.arialyy.aria.core.scheduler.UploadSchedulers;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import java.util.List;
import java.util.Set;

/**
 * Created by lyy on 2017/2/6.
 * 上传功能接收器
 */
public class UploadReceiver extends AbsReceiver<UploadEntity> {
  private static final String TAG = "UploadReceiver";
  public ISchedulerListener<UploadTask> listener;

  /**
   * 加载HTTP单文件上传任务
   *
   * @param filePath 文件路径
   */
  public UploadTarget load(@NonNull String filePath) {
    CheckUtil.checkUploadPath(filePath);
    return new UploadTarget(filePath, targetName);
  }

  /**
   * 加载FTP单文件上传任务
   *
   * @param filePath 文件路径
   */
  public FtpUploadTarget loadFtp(@NonNull String filePath) {
    CheckUtil.checkUploadPath(filePath);
    return new FtpUploadTarget(filePath, targetName);
  }

  /**
   * 通过上传路径获取上传实体
   */
  public UploadEntity getUploadEntity(String filePath) {
    return DbEntity.findFirst(UploadEntity.class, "filePath=?", filePath);
  }

  /**
   * 下载任务是否存在
   */
  @Override public boolean taskExists(String filePath) {
    return DbEntity.findFirst(UploadEntity.class, "filePath=?", filePath) != null;
  }

  @Override public List<UploadEntity> getSimpleTaskList() {
    return DbEntity.findAllData(UploadEntity.class);
  }

  @Override public void stopAllTask() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(NormalCmdFactory.getInstance()
            .createCmd(targetName, new UploadTaskEntity(), NormalCmdFactory.TASK_STOP_ALL))
        .exe();
  }

  /**
   * 删除所有任务
   *
   * @param removeFile {@code true} 删除已经上传完成的任务，不仅删除上传记录，还会删除已经上传完成的文件，{@code false}
   * 如果文件已经上传完成，只删除上传记录
   */
  @Override public void removeAllTask(boolean removeFile) {
    final AriaManager am = AriaManager.getInstance(AriaManager.APP);

    am.setCmd(CommonUtil.createCmd(targetName, new DownloadTaskEntity(),
        NormalCmdFactory.TASK_CANCEL_ALL)).exe();

    Set<String> keys = am.getReceiver().keySet();
    for (String key : keys) {
      IReceiver receiver = am.getReceiver().get(key);
      receiver.removeSchedulerListener();
      am.getReceiver().remove(key);
    }
  }

  @Override public void destroy() {
    targetName = null;
    listener = null;
  }

  /**
   * 添加调度器回调
   *
   * @see #register()
   */
  @Deprecated public UploadReceiver addSchedulerListener(ISchedulerListener<UploadTask> listener) {
    this.listener = listener;
    UploadSchedulers.getInstance().addSchedulerListener(targetName, listener);
    return this;
  }

  /**
   * @see #unRegister()
   */
  @Deprecated @Override public void removeSchedulerListener() {
    if (listener != null) {
      UploadSchedulers.getInstance().removeSchedulerListener(targetName, listener);
    }
  }

  /**
   * 将当前类注册到Aria
   */
  public UploadReceiver register() {
    String className = obj.getClass().getName();
    Set<String> cCounter = ProxyHelper.getInstance().uploadCounter;
    if (cCounter != null && cCounter.contains(className)) {
      UploadSchedulers.getInstance().register(obj);
    }
    return this;
  }

  @Override public void unRegister() {
    String className = obj.getClass().getName();
    Set<String> dCounter = ProxyHelper.getInstance().uploadCounter;
    if (dCounter != null && dCounter.contains(className)) {
      UploadSchedulers.getInstance().unRegister(obj);
    }
  }
}