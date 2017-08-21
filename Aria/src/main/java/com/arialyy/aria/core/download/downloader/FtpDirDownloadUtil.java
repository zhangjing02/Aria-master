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
import com.arialyy.aria.core.download.DownloadGroupTaskEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import java.util.Set;

/**
 * Created by Aria.Lao on 2017/7/27.
 * ftp文件夹下载工具
 */
public class FtpDirDownloadUtil extends AbsGroupUtil {
  public FtpDirDownloadUtil(IDownloadGroupListener listener, DownloadGroupTaskEntity taskEntity) {
    super(listener, taskEntity);
  }

  @Override protected void onStart() {
    super.onStart();
    if (mTaskEntity.getEntity().getFileSize() > 1) {
      startDownload();
    } else {
      new FtpDirInfoThread(mTaskEntity, new OnFileInfoCallback() {
        @Override public void onComplete(String url, int code) {
          if (code >= 200 && code < 300) {
            mTotalSize = mTaskEntity.getEntity().getFileSize();
            for (DownloadEntity entity : mTaskEntity.entity.getSubTask()) {
              mExeMap.put(entity.getUrl(), createChildDownloadTask(entity));
            }
            mActualTaskNum = mTaskEntity.entity.getSubTask().size();
            startDownload();
          }
        }

        @Override public void onFail(String url, String errorMsg) {
          mListener.onFail();
        }
      }).start();
    }
  }

  private void startDownload() {
    int i = 0;
    Set<String> keys = mExeMap.keySet();
    for (String key : keys) {
      DownloadTaskEntity taskEntity = mExeMap.get(key);
      if (taskEntity != null) {
        startChildDownload(taskEntity);
        i++;
      }
    }
    if (i == mExeMap.size()) startRunningFlow();
  }
}
