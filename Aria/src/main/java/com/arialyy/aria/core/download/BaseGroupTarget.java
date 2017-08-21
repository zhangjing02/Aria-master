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

import android.text.TextUtils;
import com.arialyy.aria.core.inf.AbsDownloadTarget;
import com.arialyy.aria.core.inf.AbsTarget;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aria.Lao on 2017/7/26.
 */
abstract class BaseGroupTarget<TARGET extends AbsTarget, TASK_ENTITY extends AbsTaskEntity>
    extends AbsDownloadTarget<TARGET, DownloadGroupEntity, TASK_ENTITY> {

  List<String> mUrls = new ArrayList<>();
  String mGroupName;
  /**
   * 子任务文件名
   */
  private List<String> mSubTaskFileName = new ArrayList<>();
  /**
   * 是否已经设置了文件路径
   */
  private boolean isSetDirPathed = false;

  /**
   * 查询任务组实体，如果数据库不存在该实体，则新创建一个新的任务组实体
   */
  DownloadGroupEntity getDownloadGroupEntity() {
    DownloadGroupEntity entity =
        DbEntity.findFirst(DownloadGroupEntity.class, "groupName=?", mGroupName);
    if (entity == null) {
      entity = new DownloadGroupEntity();
      entity.setGroupName(mGroupName);
      entity.setUrls(mUrls);
      entity.insert();
    }
    return entity;
  }

  /**
   * 设置任务组别名
   */
  public TARGET setGroupAlias(String alias) {
    if (TextUtils.isEmpty(alias)) return (TARGET) this;
    mEntity.setAlias(alias);
    mEntity.update();
    return (TARGET) this;
  }

  /**
   * 设置任务组的文件夹路径，在Aria中，任务组的所有子任务都会下载到以任务组组名的文件夹中。
   * 如：groupDirPath = "/mnt/sdcard/download/group_test"
   * <pre>
   *   {@code
   *      + mnt
   *        + sdcard
   *          + download
   *            + group_test
   *              - task1.apk
   *              - task2.apk
   *              - task3.apk
   *              ....
   *
   *   }
   * </pre>
   *
   * @param groupDirPath 任务组保存文件夹路径
   */
  public TARGET setDownloadDirPath(String groupDirPath) {
    if (TextUtils.isEmpty(groupDirPath)) {
      throw new NullPointerException("任务组文件夹保存路径不能为null");
    }

    isSetDirPathed = true;
    if (mEntity.getDirPath().equals(groupDirPath)) return (TARGET) this;

    File file = new File(groupDirPath);
    if (file.exists() && file.isFile()) {
      throw new IllegalArgumentException("路径不能为文件");
    }
    if (!file.exists()) {
      file.mkdirs();
    }

    mEntity.setDirPath(groupDirPath);
    if (!TextUtils.isEmpty(mEntity.getDirPath())) {
      reChangeDirPath(groupDirPath);
    } else {
      mEntity.setSubTasks(createSubTask());
    }
    mEntity.update();
    return (TARGET) this;
  }

  /**
   * 改变任务组文件夹路径，修改文件夹路径会将子任务所有路径更换
   *
   * @param newDirPath 新的文件夹路径
   */
  private void reChangeDirPath(String newDirPath) {
    List<DownloadEntity> subTask = mEntity.getSubTask();
    if (subTask != null && !subTask.isEmpty()) {
      for (DownloadEntity entity : subTask) {
        String oldPath = entity.getDownloadPath();
        String newPath = newDirPath + "/" + entity.getFileName();
        File file = new File(oldPath);
        file.renameTo(new File(newPath));
        DbEntity.exeSql("UPDATE DownloadEntity SET downloadPath='"
            + newPath
            + "' WHERE downloadPath='"
            + oldPath
            + "'");
        DbEntity.exeSql(
            "UPDATE DownloadTaskEntity SET key='" + newPath + "' WHERE key='" + oldPath + "'");
      }
    } else {
      mEntity.setSubTasks(createSubTask());
    }
  }

  /**
   * 设置子任务文件名，该方法必须在{@link #setDownloadDirPath(String)}之后调用，否则不生效
   */
  public TARGET setSubTaskFileName(List<String> subTaskFileName) {
    if (subTaskFileName == null || subTaskFileName.isEmpty()) return (TARGET) this;
    mSubTaskFileName.addAll(subTaskFileName);
    if (mUrls.size() != subTaskFileName.size()) {
      throw new IllegalArgumentException("下载链接数必须要和保存路径的数量一致");
    }
    if (isSetDirPathed) {
      List<DownloadEntity> entities = mEntity.getSubTask();
      int i = 0;
      for (DownloadEntity entity : entities) {
        String newName = mSubTaskFileName.get(i);
        updateSubFileName(entity, newName);
        i++;
      }
    }
    return (TARGET) this;
  }

  /**
   * 更新子任务文件名
   */
  private void updateSubFileName(DownloadEntity entity, String newName) {
    if (!newName.equals(entity.getFileName())) {
      String oldPath = mEntity.getDirPath() + "/" + entity.getFileName();
      String newPath = mEntity.getDirPath() + "/" + newName;
      File oldFile = new File(oldPath);
      if (oldFile.exists()) {
        oldFile.renameTo(new File(newPath));
      }
      CommonUtil.renameDownloadConfig(oldFile.getName(), newName);
      DbEntity.exeSql(
          "UPDATE DownloadTaskEntity SET key='" + newPath + "' WHERE key='" + oldPath + "'");
      entity.setDownloadPath(newPath);
      entity.setFileName(newName);
      entity.update();
    }
  }

  /**
   * 创建子任务
   */
  private List<DownloadEntity> createSubTask() {
    List<DownloadEntity> list = new ArrayList<>();
    for (int i = 0, len = mUrls.size(); i < len; i++) {
      DownloadEntity entity = new DownloadEntity();
      entity.setUrl(mUrls.get(i));
      String fileName = mSubTaskFileName.isEmpty() ? createFileName(entity.getUrl())
          : mSubTaskFileName.get(i);
      entity.setDownloadPath(mEntity.getDirPath() + "/" + fileName);
      entity.setGroupName(mGroupName);
      entity.setGroupChild(true);
      entity.setFileName(fileName);
      entity.insert();
      list.add(entity);
    }
    return list;
  }

}
