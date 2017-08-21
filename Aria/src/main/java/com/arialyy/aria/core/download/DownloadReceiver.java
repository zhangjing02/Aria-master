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
package com.arialyy.aria.core.download;

import android.support.annotation.NonNull;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.core.inf.AbsReceiver;
import com.arialyy.aria.core.inf.IReceiver;
import com.arialyy.aria.core.command.normal.NormalCmdFactory;
import com.arialyy.aria.core.scheduler.DownloadGroupSchedulers;
import com.arialyy.aria.core.scheduler.DownloadSchedulers;
import com.arialyy.aria.core.scheduler.ISchedulerListener;
import com.arialyy.aria.core.common.ProxyHelper;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by lyy on 2016/12/5.
 * 下载功能接收器
 */
public class DownloadReceiver extends AbsReceiver {
  private final String TAG = "DownloadReceiver";
  public ISchedulerListener<DownloadTask> listener;

  /**
   * 设置最大下载速度，单位：kb
   * 该方法为实验性功能，清不要轻易在生产环境中使用。
   *
   * @param maxSpeed 为0表示不限速
   */
  @Deprecated public void setMaxSpeed(double maxSpeed) {
    AriaManager.getInstance(AriaManager.APP).getDownloadConfig().setMsxSpeed(maxSpeed);
  }

  /**
   * 使用下载实体执行下载操作
   */
  public DownloadTarget load(DownloadEntity entity) {
    return new DownloadTarget(entity, targetName);
  }

  /**
   * 加载Http、https单任务下载地址
   */
  public DownloadTarget load(@NonNull String url) {
    CheckUtil.checkDownloadUrl(url);
    return new DownloadTarget(url, targetName);
  }

  /**
   * 加载下载地址，如果任务组的中的下载地址改变了，则任务从新的一个任务组
   */
  public DownloadGroupTarget load(List<String> urls) {
    CheckUtil.checkDownloadUrls(urls);
    return new DownloadGroupTarget(urls, targetName);
  }

  /**
   * 加载ftp单任务下载地址
   */
  public FtpDownloadTarget loadFtp(@NonNull String url) {
    CheckUtil.checkDownloadUrl(url);
    return new FtpDownloadTarget(url, targetName);
  }

  /**
   * 使用任务组实体执行任务组的实体执行任务组的下载操作
   *
   * @param groupEntity 如果加载的任务实体没有子项的下载地址，
   * 那么你需要使用{@link DownloadGroupTarget#setGroupUrl(List)}设置子项的下载地址
   */
  public DownloadGroupTarget load(DownloadGroupEntity groupEntity) {
    return new DownloadGroupTarget(groupEntity, targetName);
  }

  /**
   * 加载ftp文件夹下载地址
   */
  public FtpDirDownloadTarget loadFtpDir(@NonNull String dirUrl) {
    CheckUtil.checkDownloadUrl(dirUrl);
    return new FtpDirDownloadTarget(dirUrl, targetName);
  }

  /**
   * 将当前类注册到Aria
   */
  public DownloadReceiver register() {
    String className = obj.getClass().getName();
    Set<String> dCounter = ProxyHelper.getInstance().downloadCounter;
    Set<String> dgCounter = ProxyHelper.getInstance().downloadGroupCounter;
    if (dCounter != null && dCounter.contains(className)) {
      DownloadSchedulers.getInstance().register(obj);
    }
    if (dgCounter != null && dgCounter.contains(className)) {
      DownloadGroupSchedulers.getInstance().register(obj);
    }
    return this;
  }

  /**
   * 取消注册
   */
  @Override public void unRegister() {
    String className = obj.getClass().getName();
    Set<String> dCounter = ProxyHelper.getInstance().downloadCounter;
    Set<String> dgCounter = ProxyHelper.getInstance().downloadGroupCounter;
    if (dCounter != null && dCounter.contains(className)) {
      DownloadSchedulers.getInstance().unRegister(obj);
    }
    if (dgCounter != null && dgCounter.contains(className)) {
      DownloadGroupSchedulers.getInstance().unRegister(obj);
    }
  }

  /**
   * 添加调度器回调
   *
   * @see #register()
   */
  @Deprecated public DownloadReceiver addSchedulerListener(
      ISchedulerListener<DownloadTask> listener) {
    this.listener = listener;
    DownloadSchedulers.getInstance().addSchedulerListener(targetName, listener);
    return this;
  }

  /**
   * 移除回调
   *
   * @see #unRegister()
   */
  @Deprecated @Override public void removeSchedulerListener() {
    if (listener != null) {
      DownloadSchedulers.getInstance().removeSchedulerListener(targetName, listener);
    }
  }

  @Override public void destroy() {
    targetName = null;
    listener = null;
  }

  /**
   * 通过下载链接获取下载实体
   */
  public DownloadEntity getDownloadEntity(String downloadUrl) {
    CheckUtil.checkDownloadUrl(downloadUrl);
    return DbEntity.findFirst(DownloadEntity.class, "url=? and isGroupChild='false'",
        downloadUrl);
  }

  /**
   * 通过下载链接获取保存在数据库的下载任务实体
   */
  public DownloadTaskEntity getDownloadTask(String downloadUrl) {
    CheckUtil.checkDownloadUrl(downloadUrl);
    return DbEntity.findFirst(DownloadTaskEntity.class, "groupName=? and isGroupTask='false'",
        downloadUrl);
  }

  /**
   * 通过下载链接获取保存在数据库的下载任务组实体
   */
  public DownloadGroupTaskEntity getDownloadGroupTask(List<String> urls) {
    CheckUtil.checkDownloadUrls(urls);
    String hashCode = CommonUtil.getMd5Code(urls);
    return DbEntity.findFirst(DownloadGroupTaskEntity.class, "key=?", hashCode);
  }

  /**
   * 通过任务组key，获取任务组实体
   * 如果是http，key为所有子任务下载地址拼接后取md5
   * 如果是ftp，key为ftp服务器的文件夹路径
   */
  public DownloadGroupTaskEntity getDownloadGroupTask(String key) {
    return DbEntity.findFirst(DownloadGroupTaskEntity.class, "key=?", key);
  }

  /**
   * 下载任务是否存在
   */
  @Override public boolean taskExists(String downloadUrl) {
    return DownloadEntity.findFirst(DownloadEntity.class, "url=?", downloadUrl) != null;
  }

  /**
   * 获取普通下载任务列表
   */
  @Override public List<DownloadEntity> getSimpleTaskList() {
    return DownloadEntity.findDatas(DownloadEntity.class, "isGroupChild=? and downloadPath!=''",
        "false");
  }

  /**
   * 获取任务组列表
   */
  public List<DownloadGroupEntity> getGroupTaskList() {
    return DownloadEntity.findAllData(DownloadGroupEntity.class);
  }

  /**
   * 获取普通任务和任务组的任务列表
   */
  public List<AbsEntity> getTotleTaskList() {
    List<AbsEntity> list = new ArrayList<>();
    List<DownloadEntity> simpleTask = getSimpleTaskList();
    List<DownloadGroupEntity> groupTask = getGroupTaskList();
    if (simpleTask != null && !simpleTask.isEmpty()) {
      list.addAll(simpleTask);
    }
    if (groupTask != null && !groupTask.isEmpty()) {
      list.addAll(groupTask);
    }
    return list;
  }

  /**
   * 停止所有正在下载的任务，并清空等待队列。
   */
  @Override public void stopAllTask() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(NormalCmdFactory.getInstance()
            .createCmd(targetName, new DownloadTaskEntity(), NormalCmdFactory.TASK_STOP_ALL))
        .exe();
  }

  /**
   * 恢复所有正在下载的任务
   * 1.如果执行队列没有满，则开始下载任务，直到执行队列满
   * 2.如果队列执行队列已经满了，则将所有任务添加到等待队列中
   */
  public void resumeAllTask() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(NormalCmdFactory.getInstance()
            .createCmd(targetName, new DownloadTaskEntity(), NormalCmdFactory.TASK_RESUME_ALL))
        .exe();
  }

  /**
   * 删除所有任务
   *
   * @param removeFile {@code true} 删除已经下载完成的任务，不仅删除下载记录，还会删除已经下载完成的文件，{@code false}
   * 如果文件已经下载完成，只删除下载记录
   */
  @Override public void removeAllTask(boolean removeFile) {
    final AriaManager ariaManager = AriaManager.getInstance(AriaManager.APP);
    ariaManager.setCmd(CommonUtil.createCmd(targetName, new DownloadTaskEntity(),
        NormalCmdFactory.TASK_CANCEL_ALL)).exe();

    Set<String> keys = ariaManager.getReceiver().keySet();
    for (String key : keys) {
      ariaManager.getReceiver().remove(key);
    }
  }
}