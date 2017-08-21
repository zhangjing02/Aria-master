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
package com.arialyy.aria.core.upload;

import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.AbsUploadTarget;
import com.arialyy.aria.orm.DbEntity;
import java.io.File;

/**
 * Created by Aria.Lao on 2017/7/27.
 * ftp单任务上传
 */
public class FtpUploadTarget
    extends AbsUploadTarget<FtpUploadTarget, UploadEntity, UploadTaskEntity> {
  private final String TAG = "FtpUploadTarget";

  FtpUploadTarget(String filePath, String targetName) {
    this.mTargetName = targetName;
    mTaskEntity = DbEntity.findFirst(UploadTaskEntity.class, "key=?", filePath);
    if (mTaskEntity == null) {
      mTaskEntity = new UploadTaskEntity();
      mTaskEntity.entity = getUploadEntity(filePath);
    }
    if (mTaskEntity.entity == null) {
      mTaskEntity.entity = getUploadEntity(filePath);
    }
    mTaskEntity.requestType = AbsTaskEntity.FTP;
    mEntity = mTaskEntity.entity;
    File file = new File(filePath);
    mEntity.setFileName(file.getName());
    mEntity.setFileSize(file.length());
  }

  /**
   * ftp 用户登录信息
   *
   * @param userName ftp用户名
   * @param password ftp用户密码
   */
  public FtpUploadTarget login(String userName, String password) {
    return login(userName, password, null);
  }

  /**
   * ftp 用户登录信息
   *
   * @param userName ftp用户名
   * @param password ftp用户密码
   * @param account ftp账号
   */
  public FtpUploadTarget login(String userName, String password, String account) {
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
