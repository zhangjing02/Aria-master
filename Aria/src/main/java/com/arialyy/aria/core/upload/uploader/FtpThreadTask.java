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

import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.common.AbsThreadTask;
import com.arialyy.aria.core.common.StateConstance;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.inf.IEventListener;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Created by Aria.Lao on 2017/7/28.
 * FTP 单线程上传任务，需要FTP 服务器给用户打开删除和读入IO的权限
 */
class FtpThreadTask extends AbsThreadTask<UploadEntity, UploadTaskEntity> {
  private final String TAG = "FtpThreadTask";
  private String dir, remotePath, charSet;

  FtpThreadTask(StateConstance constance, IEventListener listener,
      SubThreadConfig<UploadTaskEntity> info) {
    super(constance, listener, info);
  }

  @Override public void run() {
    FTPClient client = null;
    OutputStream os = null;
    BufferedRandomAccessFile file = null;
    try {
      Log.d(TAG, "任务【"
          + mConfig.TEMP_FILE.getName()
          + "】线程__"
          + mConfig.THREAD_ID
          + "__开始上传【开始位置 : "
          + mConfig.START_LOCATION
          + "，结束位置："
          + mConfig.END_LOCATION
          + "】");
      //当前子线程的下载位置
      mChildCurrentLocation = mConfig.START_LOCATION;
      client = createClient();
      if (client == null) return;
      client.makeDirectory(dir);
      client.changeWorkingDirectory(dir);
      client.setRestartOffset(mConfig.START_LOCATION);
      file = new BufferedRandomAccessFile(mConfig.TEMP_FILE, "rwd", mBufSize);
      file.seek(mConfig.START_LOCATION);
      if (!isRemoteComplete(client)) {
        os = client.storeFileStream(new String(remotePath.getBytes(charSet), SERVER_CHARSET));
        //发送第二次指令时，还需要再做一次判断
        int reply = client.getReplyCode();
        if (!FTPReply.isPositivePreliminary(reply)) {
          client.disconnect();
          fail(mChildCurrentLocation, "上传文件错误，错误码为：" + reply, null);
          return;
        }
        upload(file, os);
      }
      if (STATE.isCancel || STATE.isStop) return;
      Log.i(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】线程__" + mConfig.THREAD_ID + "__上传完毕");
      writeConfig(true, 1);
      STATE.COMPLETE_THREAD_NUM++;
      if (STATE.isComplete()) {
        File configFile = new File(mConfigFPath);
        if (configFile.exists()) {
          configFile.delete();
        }
        STATE.isRunning = false;
        mListener.onComplete();
      }
    } catch (IOException e) {
      fail(mChildCurrentLocation, "上传失败【" + mConfig.URL + "】", e);
    } catch (Exception e) {
      fail(mChildCurrentLocation, "获取流失败", e);
    } finally {
      try {
        if (file != null) {
          file.close();
        }
        if (os != null) {
          os.close();
        }
        if (client != null && client.isConnected()) {
          client.disconnect();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 远程文件是否已经玩飞车
   *
   * @return true 任务已经完成
   */
  private boolean isRemoteComplete(FTPClient client) throws IOException {
    FTPFile[] files = client.listFiles(new String(remotePath.getBytes(charSet), SERVER_CHARSET));
    return files.length != 0 && files[0].getSize() == mEntity.getFileSize();
  }

  /**
   * 执行上传操作
   */
  private void upload(BufferedRandomAccessFile file, OutputStream os)
      throws IOException, InterruptedException {
    int len;
    byte[] buffer = new byte[mBufSize];
    while ((len = file.read(buffer)) != -1) {
      if (STATE.isCancel) break;
      if (STATE.isStop) break;
      if (mSleepTime > 0) Thread.sleep(mSleepTime);
      if (mChildCurrentLocation + len >= mConfig.END_LOCATION) {
        len = (int) (mConfig.END_LOCATION - mChildCurrentLocation);
        os.write(buffer, 0, len);
        progress(len);
        break;
      } else {
        os.write(buffer, 0, len);
        progress(len);
      }
    }
  }

  /**
   * 构建FTP客户端
   */
  private FTPClient createClient() throws IOException {
    String url = mEntity.getUrl();
    String[] pp = url.split("/")[2].split(":");
    String serverIp = pp[0];
    int port = Integer.parseInt(pp[1]);
    dir = url.substring(url.indexOf(pp[1]) + pp[1].length(), url.length());
    remotePath = dir + "/" + mEntity.getFileName();
    FTPClient client = new FTPClient();
    client.connect(serverIp, port);
    if (!TextUtils.isEmpty(mTaskEntity.account)) {
      client.login(mTaskEntity.userName, mTaskEntity.userPw);
    } else {
      client.login(mTaskEntity.userName, mTaskEntity.userPw, mTaskEntity.account);
    }
    int reply = client.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply)) {
      client.disconnect();
      fail(STATE.CURRENT_LOCATION, "无法连接到ftp服务器，错误码为：" + reply, null);
      return null;
    }
    charSet = "UTF-8";
    // 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码
    if (!TextUtils.isEmpty(mTaskEntity.charSet) || !FTPReply.isPositiveCompletion(
        client.sendCommand("OPTS UTF8", "ON"))) {
      charSet = mTaskEntity.charSet;
    }
    client.setControlEncoding(charSet);
    client.setDataTimeout(STATE.READ_TIME_OUT);
    client.enterLocalPassiveMode();
    client.setFileType(FTP.BINARY_FILE_TYPE);
    client.allocate(mBufSize);
    return client;
  }
}
