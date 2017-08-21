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

import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.util.CommonUtil;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * 下载文件信息获取
 */
class HttpFileInfoThread implements Runnable {
  private final String TAG = "HttpFileInfoThread";
  private DownloadEntity mEntity;
  private DownloadTaskEntity mTaskEntity;
  private int mConnectTimeOut;
  private OnFileInfoCallback onFileInfoListener;

  HttpFileInfoThread(DownloadTaskEntity taskEntity, OnFileInfoCallback callback) {
    this.mTaskEntity = taskEntity;
    mEntity = taskEntity.getEntity();
    mConnectTimeOut =
        AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getConnectTimeOut();
    onFileInfoListener = callback;
  }

  @Override public void run() {
    HttpURLConnection conn = null;
    try {
      URL url = new URL(CommonUtil.convertUrl(mEntity.getUrl()));
      conn = ConnectionHelp.handleConnection(url);
      conn = ConnectionHelp.setConnectParam(mTaskEntity, conn);
      conn.setRequestProperty("Range", "bytes=" + 0 + "-");
      conn.setConnectTimeout(mConnectTimeOut);
      conn.connect();
      handleConnect(conn);
    } catch (IOException e) {
      failDownload("下载失败【downloadUrl:"
          + mEntity.getUrl()
          + "】\n【filePath:"
          + mEntity.getDownloadPath()
          + "】\n"
          + CommonUtil.getPrintException(e));
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  private void handleConnect(HttpURLConnection conn) throws IOException {
    long len = conn.getContentLength();
    if (len < 0) {
      String temp = conn.getHeaderField(mTaskEntity.contentLength);
      len = TextUtils.isEmpty(temp) ? -1 : Long.parseLong(temp);
    }
    int code = conn.getResponseCode();
    boolean isComplete = false;
    if (TextUtils.isEmpty(mEntity.getMd5Code())) {
      String md5Code = conn.getHeaderField(mTaskEntity.md5Key);
      mEntity.setMd5Code(md5Code);
    }
    String disposition = conn.getHeaderField(mTaskEntity.dispositionKey);
    //Map<String, List<String>> headers = conn.getHeaderFields();
    if (!TextUtils.isEmpty(disposition)) {
      mEntity.setDisposition(CommonUtil.encryptBASE64(disposition));
      if (disposition.contains(mTaskEntity.dispositionFileKey)) {
        String[] infos = disposition.split("=");
        mEntity.setServerFileName(URLDecoder.decode(infos[1], "utf-8"));
      }
    }

    mTaskEntity.code = code;
    if (code == HttpURLConnection.HTTP_PARTIAL) {
      if (!checkLen(len)) return;
      mEntity.setFileSize(len);
      mTaskEntity.isSupportBP = true;
      isComplete = true;
    } else if (code == HttpURLConnection.HTTP_OK) {
      if (!checkLen(len)) return;
      mEntity.setFileSize(len);
      mTaskEntity.isSupportBP = false;
      isComplete = true;
    } else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
      failDownload("任务【" + mEntity.getUrl() + "】下载失败，错误码：404");
    } else if (code == HttpURLConnection.HTTP_MOVED_TEMP
        || code == HttpURLConnection.HTTP_MOVED_PERM
        || code == HttpURLConnection.HTTP_SEE_OTHER) {
      mTaskEntity.redirectUrl = conn.getHeaderField(mTaskEntity.redirectUrlKey);
      mEntity.setRedirect(true);
      mEntity.setRedirectUrl(mTaskEntity.redirectUrl);
      handle302Turn(conn);
    } else {
      failDownload("任务【" + mEntity.getUrl() + "】下载失败，错误码：" + code);
    }
    if (isComplete) {
      if (onFileInfoListener != null) {
        onFileInfoListener.onComplete(mEntity.getUrl(), code);
      }
      mEntity.update();
      mTaskEntity.update();
    }
  }

  /**
   * 处理30x跳转
   */
  private void handle302Turn(HttpURLConnection conn) throws IOException {
    String newUrl = conn.getHeaderField(mTaskEntity.redirectUrlKey);
    Log.d(TAG, "30x跳转，location【 " + mTaskEntity.redirectUrlKey + "】" + "新url为【" + newUrl + "】");
    if (TextUtils.isEmpty(newUrl) || newUrl.equalsIgnoreCase("null")) {
      if (onFileInfoListener != null) {
        onFileInfoListener.onFail(mEntity.getUrl(), "获取重定向链接失败");
      }
      return;
    }
    String cookies = conn.getHeaderField("Set-Cookie");
    conn = (HttpURLConnection) new URL(newUrl).openConnection();
    conn = ConnectionHelp.setConnectParam(mTaskEntity, conn);
    conn.setRequestProperty("Cookie", cookies);
    conn.setRequestProperty("Range", "bytes=" + 0 + "-");
    conn.setConnectTimeout(mConnectTimeOut);
    conn.connect();
    handleConnect(conn);
    conn.disconnect();
  }

  /**
   * 检查长度是否合法
   *
   * @param len 从服务器获取的文件长度
   * @return true, 合法
   */
  private boolean checkLen(long len) {
    if (len < 0) {
      failDownload("任务【" + mEntity.getUrl() + "】下载失败，文件长度小于0");
      return false;
    }
    return true;
  }

  private void failDownload(String errorMsg) {
    Log.e(TAG, errorMsg);
    if (onFileInfoListener != null) {
      onFileInfoListener.onFail(mEntity.getUrl(), errorMsg);
    }
  }
}