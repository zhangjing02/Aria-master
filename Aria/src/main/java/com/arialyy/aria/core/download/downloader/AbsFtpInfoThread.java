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
import com.arialyy.aria.core.common.AbsThreadTask;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.util.CommonUtil;
import java.io.IOException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Created by Aria.Lao on 2017/7/25.
 * 获取ftp文件夹信息
 */
abstract class AbsFtpInfoThread<ENTITY extends AbsEntity, TASK_ENTITY extends AbsTaskEntity<ENTITY>>
    implements Runnable {

  private final String TAG = "HttpFileInfoThread";
  protected ENTITY mEntity;
  protected TASK_ENTITY mTaskEntity;
  private int mConnectTimeOut;
  private OnFileInfoCallback mCallback;

  AbsFtpInfoThread(TASK_ENTITY taskEntity, OnFileInfoCallback callback) {
    mTaskEntity = taskEntity;
    mEntity = taskEntity.getEntity();
    mConnectTimeOut =
        AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getConnectTimeOut();
    mCallback = callback;
  }

  @Override public void run() {
    FTPClient client = null;
    try {
      String url = mTaskEntity.getEntity().getKey();
      String[] pp = url.split("/")[2].split(":");
      String serverIp = pp[0];
      int port = Integer.parseInt(pp[1]);
      String remotePath = url.substring(url.indexOf(pp[1]) + pp[1].length(), url.length());
      client = new FTPClient();
      client.connect(serverIp, port);
      if (!TextUtils.isEmpty(mTaskEntity.account)) {
        client.login(mTaskEntity.userName, mTaskEntity.userPw);
      } else {
        client.login(mTaskEntity.userName, mTaskEntity.userPw, mTaskEntity.account);
      }
      int reply = client.getReplyCode();
      if (!FTPReply.isPositiveCompletion(reply)) {
        client.disconnect();
        failDownload("无法连接到ftp服务器，错误码为：" + reply);
        return;
      }
      client.setDataTimeout(mConnectTimeOut);
      String charSet = "UTF-8";
      // 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码
      if (!TextUtils.isEmpty(mTaskEntity.charSet) || !FTPReply.isPositiveCompletion(
          client.sendCommand("OPTS UTF8", "ON"))) {
        charSet = mTaskEntity.charSet;
      }
      client.setControlEncoding(charSet);
      client.enterLocalPassiveMode();
      client.setFileType(FTP.BINARY_FILE_TYPE);
      FTPFile[] files =
          client.listFiles(new String(remotePath.getBytes(charSet), AbsThreadTask.SERVER_CHARSET));
      long size = getFileSize(files, client, remotePath);
      mEntity.setFileSize(size);
      reply = client.getReplyCode();
      if (!FTPReply.isPositiveCompletion(reply)) {
        client.disconnect();
        failDownload("获取文件信息错误，错误码为：" + reply);
        return;
      }
      mTaskEntity.code = reply;
      onPreComplete();
      mEntity.update();
      mTaskEntity.update();
      mCallback.onComplete(mEntity.getKey(), reply);
    } catch (IOException e) {
      failDownload(e.getMessage());
    } finally {
      if (client != null) {
        try {
          client.disconnect();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  void start() {
    new Thread(this).start();
  }

  protected void onPreComplete() {

  }

  /**
   * 遍历FTP服务器上对应文件或文件夹大小
   *
   * @throws IOException
   */
  private long getFileSize(FTPFile[] files, FTPClient client, String dirName) throws IOException {
    long size = 0;
    String path = dirName + "/";
    for (FTPFile file : files) {
      if (file.isFile()) {
        size += file.getSize();
        handleFile(path + file.getName(), file);
      } else {
        size += getFileSize(client.listFiles(
            CommonUtil.strCharSetConvert(path + file.getName(), mTaskEntity.charSet)), client,
            path + file.getName());
      }
    }
    return size;
  }

  /**
   * 处理FTP文件信息
   *
   * @param remotePath ftp服务器文件夹路径
   * @param ftpFile ftp服务器上对应的文件
   */
  void handleFile(String remotePath, FTPFile ftpFile) {
  }

  private void failDownload(String errorMsg) {
    Log.e(TAG, errorMsg);
    if (mCallback != null) {
      mCallback.onFail(mEntity.getKey(), errorMsg);
    }
  }
}
