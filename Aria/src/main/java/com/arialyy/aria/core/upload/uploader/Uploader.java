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
package com.arialyy.aria.core.upload.uploader;

import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.common.AbsFileer;
import com.arialyy.aria.core.common.AbsThreadTask;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IUploadListener;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;

/**
 * Created by Aria.Lao on 2017/7/27.
 * 文件上传器
 */
class Uploader extends AbsFileer<UploadEntity, UploadTaskEntity> {

  Uploader(IUploadListener listener, UploadTaskEntity taskEntity) {
    super(listener, taskEntity);
    mTempFile = new File(mEntity.getFilePath());
  }

  /**
   * 检查任务是否是新任务，新任务条件：
   * 1、文件不存在
   * 2、记录文件不存在
   * 3、记录文件缺失或不匹配
   * 4、数据库记录不存在
   * 5、不支持断点，则是新任务
   */
  protected void checkTask() {
    mConfigFile = new File(mContext.getFilesDir().getPath()
        + AriaManager.UPLOAD_TEMP_DIR
        + mEntity.getFileName()
        + ".properties");
    if (!mTaskEntity.isSupportBP) {
      isNewTask = true;
      return;
    }
    if (!mConfigFile.exists()) { //记录文件被删除，则重新下载
      isNewTask = true;
      CommonUtil.createFile(mConfigFile.getPath());
    } else if (DbEntity.findFirst(UploadEntity.class, "filePath=?", mEntity.getFilePath())
        == null) {
      isNewTask = true;
    } else {
      isNewTask = checkConfigFile();
    }
  }

  @Override protected void handleNewTask() {

  }

  @Override protected int getNewTaskThreadNum() {
    return 1;
  }

  @Override protected AbsThreadTask selectThreadTask(SubThreadConfig<UploadTaskEntity> config) {
    switch (mTaskEntity.requestType) {
      case AbsTaskEntity.FTP:
      case AbsTaskEntity.FTP_DIR:
        return new FtpThreadTask(mConstance, mListener, config);
      case AbsTaskEntity.HTTP:
        return new HttpThreadTask(mConstance, (IUploadListener) mListener, config);
    }
    return null;
  }
}
