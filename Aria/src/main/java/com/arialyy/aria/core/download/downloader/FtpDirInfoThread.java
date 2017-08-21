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
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.download.DownloadGroupTaskEntity;
import com.arialyy.aria.util.CommonUtil;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.apache.commons.net.ftp.FTPFile;

/**
 * Created by Aria.Lao on 2017/7/25.
 * 获取ftp文件夹信息
 */
class FtpDirInfoThread extends AbsFtpInfoThread<DownloadGroupEntity, DownloadGroupTaskEntity> {
  private long mSize = 0;

  FtpDirInfoThread(DownloadGroupTaskEntity taskEntity, OnFileInfoCallback callback) {
    super(taskEntity, callback);
  }

  @Override void handleFile(String remotePath, FTPFile ftpFile) {
    super.handleFile(remotePath, ftpFile);
    mSize += ftpFile.getSize();
    addEntity(remotePath, ftpFile);
  }

  @Override protected void onPreComplete() {
    super.onPreComplete();
    mEntity.setFileSize(mSize);
  }

  private void addEntity(String remotePath, FTPFile ftpFile) {
    DownloadEntity entity = new DownloadEntity();
    entity.setUrl("ftp://" + mTaskEntity.serverIp + ":" + mTaskEntity.port + remotePath);
    entity.setDownloadPath(mEntity.getDirPath() + "/" + remotePath);
    int lastIndex = remotePath.lastIndexOf("/");
    String fileName = lastIndex < 0 ? CommonUtil.keyToHashKey(remotePath)
        : remotePath.substring(lastIndex + 1, remotePath.length());
    entity.setFileName(new String(fileName.getBytes(), Charset.forName(mTaskEntity.charSet)));
    entity.setGroupName(mEntity.getGroupName());
    entity.setGroupChild(true);
    entity.setFileSize(ftpFile.getSize());
    entity.insert();
    if (mEntity.getUrls() == null) {
      mEntity.setUrls(new ArrayList<String>());
    }
    if (mEntity.getSubTask() == null) {
      mEntity.setSubTasks(new ArrayList<DownloadEntity>());
    }
    mEntity.getSubTask().add(entity);
  }
}
