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
import java.io.IOException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Created by Aria.Lao on 2017/7/26.
 */
public class FtpClientHelp {
  private final String TAG = "FtpClientHelp";
  private static volatile FtpClientHelp INSTANCE = null;

  private FTPClient client;
  private String serverIp, user, pw, account;
  private int port;

  private FtpClientHelp() {
  }

  public static FtpClientHelp getInstnce() {
    if (INSTANCE == null) {
      synchronized (AriaManager.LOCK) {
        INSTANCE = new FtpClientHelp();
      }
    }
    return INSTANCE;
  }

  public FTPClient getClient() {
    if (client == null || !client.isConnected()) {
      createClient();
    }
    return client;
  }

  /**
   * 登录到FTP服务器，当客户端为null或客户端没有连接到FTP服务器时才会执行登录操作
   */
  public FTPClient login(String serverIp, int port, String user, String pw, String account) {
    this.serverIp = serverIp;
    this.port = port;
    this.user = user;
    this.pw = pw;
    this.account = account;
    if (client == null || !client.isConnected()) {
      createClient();
    }
    return client;
  }

  /**
   * 登出
   */
  public void logout() {
    try {
      if (client != null && client.isConnected()) {
        client.logout();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  FTPClient createClient() {
    new Thread(new Runnable() {
      @Override public void run() {
        client = new FTPClient();
        try {
          client.connect(serverIp, port);
          if (!TextUtils.isEmpty(account)) {
            client.login(user, pw);
          } else {
            client.login(user, pw, account);
          }
          int reply = client.getReplyCode();
          if (!FTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            Log.e(TAG, "无法连接到ftp服务器，错误码为：" + reply);
          }
        } catch (IOException e) {
          Log.d(TAG, e.getMessage());
        } finally {
          synchronized (FtpClientHelp.this) {
            FtpClientHelp.this.notify();
          }
        }
      }
    }).start();
    synchronized (FtpClientHelp.this) {
      try {
        wait();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return client;
  }
}
