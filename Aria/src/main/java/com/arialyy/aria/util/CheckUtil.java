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

package com.arialyy.aria.util;

import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import com.arialyy.aria.exception.FileException;
import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lyy on 2016/9/23.
 * 检查帮助类
 */
public class CheckUtil {
  private static final String TAG = "CheckUtil";

  /**
   * 判空
   */
  public static void checkNull(Object obj) {
    if (obj == null) throw new IllegalArgumentException("不能传入空对象");
  }

  /**
   * 检查sql的expression是否合法
   */
  public static void checkSqlExpression(String... expression) {
    if (expression.length == 0) {
      throw new IllegalArgumentException("sql语句表达式不能为null");
    }
    if (expression.length == 1) {
      throw new IllegalArgumentException("表达式需要写入参数");
    }
    String where = expression[0];
    if (!where.contains("?")) {
      throw new IllegalArgumentException("请在where语句的'='后编写?");
    }
    Pattern pattern = Pattern.compile("\\?");
    Matcher matcher = pattern.matcher(where);
    int count = 0;
    while (matcher.find()) {
      count++;
    }
    if (count < expression.length - 1) {
      throw new IllegalArgumentException("条件语句的?个数不能小于参数个数");
    }
    if (count > expression.length - 1) {
      throw new IllegalArgumentException("条件语句的?个数不能大于参数个数");
    }
  }

  /**
   * 检测下载链接是否为null
   */
  public static void checkDownloadUrl(String downloadUrl) {
    if (TextUtils.isEmpty(downloadUrl)) throw new IllegalArgumentException("下载链接不能为null");
  }

  /**
   * 检测下载链接是否为null
   */
  public static void checkUploadUrl(String downloadUrl) {
    if (TextUtils.isEmpty(downloadUrl)) throw new IllegalArgumentException("上传地址不能为null");
  }


  /**
   * 检测下载链接组是否为null
   */
  public static void checkDownloadUrls(List<String> urls) {
    if (urls == null || urls.isEmpty()) {
      throw new IllegalArgumentException("链接组不能为null");
    }
  }

  /**
   * 检查下载任务组保存路径
   */
  public static void checkDownloadPaths(List<String> paths) {
    if (paths == null || paths.isEmpty()) {
      throw new IllegalArgumentException("链接保存路径不能为null");
    }
  }

  /**
   * 检测上传地址是否为null
   */
  public static void checkUploadPath(String uploadPath) {
    if (TextUtils.isEmpty(uploadPath)) throw new IllegalArgumentException("上传地址不能为null");
    File file = new File(uploadPath);
    if (!file.exists()) throw new IllegalArgumentException("上传文件不存在");
  }

  /**
   * 检查任务实体
   */
  public static void checkTaskEntity(AbsTaskEntity entity) {
    if (entity instanceof DownloadTaskEntity) {
      checkDownloadTaskEntity(((DownloadTaskEntity) entity).getEntity());
    } else if (entity instanceof UploadTaskEntity) {
      checkUploadTaskEntity(((UploadTaskEntity) entity).getEntity());
    }
  }

  /**
   * 检查命令实体
   *
   * @param checkType 删除命令和停止命令不需要检查下载链接和保存路径
   * @return {@code false}实体无效
   */
  public static boolean checkCmdEntity(AbsTaskEntity entity, boolean checkType) {
    boolean b = false;
    if (entity instanceof DownloadTaskEntity) {
      DownloadEntity entity1 = ((DownloadTaskEntity) entity).getEntity();
      if (entity1 == null) {
        Log.e(TAG, "下载实体不能为空");
      } else if (checkType && TextUtils.isEmpty(entity1.getUrl())) {
        Log.e(TAG, "下载链接不能为空");
      } else if (checkType && TextUtils.isEmpty(entity1.getDownloadPath())) {
        Log.e(TAG, "保存路径不能为空");
      } else {
        b = true;
      }
    } else if (entity instanceof UploadTaskEntity) {
      UploadEntity entity1 = ((UploadTaskEntity) entity).getEntity();
      if (entity1 == null) {
        Log.e(TAG, "上传实体不能为空");
      } else if (TextUtils.isEmpty(entity1.getFilePath())) {
        Log.e(TAG, "上传文件路径不能为空");
      } else {
        b = true;
      }
    }
    return b;
  }

  /**
   * 检查上传实体是否合法
   */
  private static void checkUploadTaskEntity(UploadEntity entity) {
    if (entity == null) {
      throw new NullPointerException("上传实体不能为空");
    } else if (TextUtils.isEmpty(entity.getFilePath())) {
      throw new IllegalArgumentException("上传文件路径不能为空");
    } else if (TextUtils.isEmpty(entity.getFileName())) {
      throw new IllegalArgumentException("上传文件名不能为空");
    }
  }

  /**
   * 检测下载实体是否合法
   * 合法(true)
   *
   * @param entity 下载实体
   */
  private static void checkDownloadTaskEntity(DownloadEntity entity) {
    if (entity == null) {
      throw new NullPointerException("下载实体不能为空");
    } else if (TextUtils.isEmpty(entity.getUrl())) {
      throw new IllegalArgumentException("下载链接不能为空");
    } else if (TextUtils.isEmpty(entity.getFileName())) {
      throw new FileException("文件名不能为null");
    } else if (TextUtils.isEmpty(entity.getDownloadPath())) {
      throw new FileException("文件保存路径不能为null");
    }
  }
}