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
import android.util.Log;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.orm.DbEntity;

/**
 * Created by Aria.Lao on 2017/7/26.
 * ftp文件夹下载
 */
public class FtpDirDownloadTarget
    extends BaseGroupTarget<FtpDirDownloadTarget, DownloadGroupTaskEntity> {
  private final String TAG = "FtpDirDownloadTarget";
  private String serverIp, remotePath;
  private int port;

  FtpDirDownloadTarget(String url, String targetName) {
    init(url);
    String[] pp = url.split("/")[2].split(":");
    mTargetName = targetName;
    serverIp = pp[0];
    port = Integer.parseInt(pp[1]);
    mTaskEntity.requestType = AbsTaskEntity.FTP_DIR;
    mTaskEntity.serverIp = serverIp;
    mTaskEntity.port = port;
    remotePath = url.substring(url.indexOf(pp[1]) + pp[1].length(), url.length());
    if (TextUtils.isEmpty(remotePath)) {
      throw new NullPointerException("ftp服务器地址不能为null");
    }
  }

  private void init(String key) {
    mGroupName = key;
    mTaskEntity = DbEntity.findFirst(DownloadGroupTaskEntity.class, "key=?", key);
    if (mTaskEntity == null) {
      mTaskEntity = new DownloadGroupTaskEntity();
      mTaskEntity.key = key;
      mTaskEntity.entity = getDownloadGroupEntity();
      mTaskEntity.insert();
    }
    if (mTaskEntity.entity == null) {
      mTaskEntity.entity = getDownloadGroupEntity();
    }
    mEntity = mTaskEntity.entity;
  }

  /**
   * 设置字符编码
   */
  public FtpDirDownloadTarget charSet(String charSet) {
    if (TextUtils.isEmpty(charSet)) return this;
    mTaskEntity.charSet = charSet;
    return this;
  }

  /**
   * ftp 用户登录信息
   *
   * @param userName ftp用户名
   * @param password ftp用户密码
   */
  public FtpDirDownloadTarget login(String userName, String password) {
    return login(userName, password, null);
  }

  /**
   * ftp 用户登录信息
   *
   * @param userName ftp用户名
   * @param password ftp用户密码
   * @param account ftp账号
   */
  public FtpDirDownloadTarget login(String userName, String password, String account) {
    if (TextUtils.isEmpty(userName)) {
      Log.e(TAG, "用户名不能为null");
      return this;
    } else if (TextUtils.isEmpty(password)) {
      Log.e(TAG, "密码不能为null");
      return this;
    }
    mTaskEntity.userName = userName;
    mTaskEntity.userPw = password;
    mTaskEntity.account = account;
    return this;
  }
}
