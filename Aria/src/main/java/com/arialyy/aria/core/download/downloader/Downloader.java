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

import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.common.AbsFileer;
import com.arialyy.aria.core.common.AbsThreadTask;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IDownloadListener;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.io.IOException;

/**
 * Created by AriaL on 2017/7/1.
 * 文件下载器
 */
class Downloader extends AbsFileer<DownloadEntity, DownloadTaskEntity> {

  Downloader(IDownloadListener listener, DownloadTaskEntity taskEntity) {
    super(listener, taskEntity);
  }

  @Override protected void checkTask() {
    if (!mTaskEntity.isSupportBP) {
      isNewTask = true;
      return;
    }
    mConfigFile = new File(mContext.getFilesDir().getPath()
        + AriaManager.DOWNLOAD_TEMP_DIR
        + mEntity.getFileName()
        + ".properties");
    mTempFile = new File(mEntity.getDownloadPath());
    if (!mConfigFile.exists()) { //记录文件被删除，则重新下载
      isNewTask = true;
      CommonUtil.createFile(mConfigFile.getPath());
    } else if (!mTempFile.exists()) {
      isNewTask = true;
    } else if (DbEntity.findFirst(DownloadEntity.class, "url=?", mEntity.getDownloadUrl())
        == null) {
      isNewTask = true;
    } else {
      isNewTask = checkConfigFile();
    }
  }

  @Override protected void handleNewTask() {
    CommonUtil.createFile(mTempFile.getPath());
    BufferedRandomAccessFile file = null;
    try {
      file = new BufferedRandomAccessFile(new File(mTempFile.getPath()), "rwd", 8192);
      //设置文件长度
      file.setLength(mEntity.getFileSize());
    } catch (IOException e) {
      failDownload("下载失败【downloadUrl:"
          + mEntity.getUrl()
          + "】\n【filePath:"
          + mEntity.getDownloadPath()
          + "】\n"
          + CommonUtil.getPrintException(e));
    } finally {
      if (file != null) {
        try {
          file.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override protected AbsThreadTask selectThreadTask(SubThreadConfig<DownloadTaskEntity> config) {
    switch (mTaskEntity.requestType) {
      case AbsTaskEntity.FTP:
      case AbsTaskEntity.FTP_DIR:
        return new FtpThreadTask(mConstance, (IDownloadListener) mListener, config);
      case AbsTaskEntity.HTTP:
        return new HttpThreadTask(mConstance, (IDownloadListener) mListener, config);
    }
    return null;
  }
}
