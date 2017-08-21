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
package com.arialyy.aria.core.inf;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.command.normal.NormalCmdFactory;
import com.arialyy.aria.util.CommonUtil;
import java.util.Map;
import java.util.Set;

/**
 * Created by AriaL on 2017/7/3.
 */
public abstract class AbsTarget<TARGET extends AbsTarget, ENTITY extends AbsEntity, TASK_ENTITY extends AbsTaskEntity>
    implements ITarget<TARGET> {
  protected ENTITY mEntity;
  protected TASK_ENTITY mTaskEntity;
  protected String mTargetName;

  /**
   * 删除记录
   */
  public void removeRecord() {
    mEntity.deleteData();
  }

  /**
   * 任务是否存在
   */
  public boolean taskExists() {
    return false;
  }

  /**
   * 获取任务进度，如果任务存在，则返回当前进度
   *
   * @return 该任务进度
   */
  public long getCurrentProgress() {
    return mEntity == null ? -1 : mEntity.getCurrentProgress();
  }

  /**
   * 获取任务文件大小
   *
   * @return 文件大小
   */
  @Override public long getSize() {
    return mEntity == null ? 0 : mEntity.getFileSize();
  }

  /**
   * 获取单位转换后的文件大小
   *
   * @return 文件大小{@code xxx mb}
   */
  @Override public String getConvertSize() {
    return mEntity == null ? "0b" : CommonUtil.formatFileSize(mEntity.getFileSize());
  }

  /**
   * 设置扩展字段，用来保存你的其它数据，如果你的数据比较多，你可以把你的数据转换为JSON字符串，然后再存到Aria中
   *
   * @param str 扩展数据
   */
  public TARGET setExtendField(String str) {
    if (TextUtils.isEmpty(str)) return (TARGET) this;
    if (TextUtils.isEmpty(mEntity.getStr()) || !mEntity.getStr().equals(str)) {
      mEntity.setStr(str);
      mEntity.save();
    }
    return (TARGET) this;
  }

  /**
   * 获取存放的扩展字段
   * 设置扩展字段{@link #setExtendField(String)}
   */
  public String getExtendField() {
    return mEntity.getStr();
  }

  /**
   * 获取任务状态
   *
   * @return {@link IEntity}
   */
  public int getTaskState() {
    return mEntity.getState();
  }

  /**
   * 获取任务进度百分比
   *
   * @return 返回任务进度
   */
  @Override public int getPercent() {
    if (mEntity == null) {
      Log.e("AbsTarget", "下载管理器中没有该任务");
      return 0;
    }
    if (mEntity.getFileSize() != 0) {
      return (int) (mEntity.getCurrentProgress() * 100 / mEntity.getFileSize());
    }
    return 0;
  }

  /**
   * 给url请求添加头部
   *
   * @param key 头部key
   * @param header 头部value
   */
  public TARGET addHeader(@NonNull String key, @NonNull String header) {
    mTaskEntity.headers.put(key, header);
    return (TARGET) this;
  }

  /**
   * 给url请求添加头部
   */
  public TARGET addHeaders(Map<String, String> headers) {
    if (headers != null && headers.size() > 0) {
      Set<String> keys = headers.keySet();
      for (String key : keys) {
        mTaskEntity.headers.put(key, headers.get(key));
      }
    }
    return (TARGET) this;
  }

  /**
   * 设置请求类型
   *
   * @param requestEnum {@link RequestEnum}
   */
  public TARGET setRequestMode(RequestEnum requestEnum) {
    mTaskEntity.requestEnum = requestEnum;
    return (TARGET) this;
  }

  /**
   * 开始任务
   */
  @Override public void start() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(mTargetName, mTaskEntity, NormalCmdFactory.TASK_START))
        .exe();
  }

  /**
   * 停止任务
   *
   * @see #stop()
   */
  @Deprecated public void pause() {
    stop();
  }

  @Override public void stop() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(mTargetName, mTaskEntity, NormalCmdFactory.TASK_STOP))
        .exe();
  }

  /**
   * 恢复任务
   */
  @Override public void resume() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(mTargetName, mTaskEntity, NormalCmdFactory.TASK_START))
        .exe();
  }

  /**
   * 删除任务
   */
  @Override public void cancel() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(mTargetName, mTaskEntity, NormalCmdFactory.TASK_CANCEL))
        .exe();
  }

  /**
   * 删除任务
   *
   * @param removeFile {@code true} 不仅删除任务数据库记录，还会删除已经删除完成的文件
   * {@code false}如果任务已经完成，只删除任务数据库记录，
   */
  public void cancel(boolean removeFile) {
    mTaskEntity.removeFile = removeFile;
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(mTargetName, mTaskEntity, NormalCmdFactory.TASK_CANCEL))
        .exe();
  }

  /**
   * 创建文件名，如果url链接有后缀名，则使用url中的后缀名
   *
   * @return url 的 hashKey
   */
  protected String createFileName(String url) {
    int end = url.indexOf("?");
    String tempUrl, fileName = "";
    if (end > 0) {
      tempUrl = url.substring(0, end);
      int tempEnd = tempUrl.lastIndexOf("/");
      if (tempEnd > 0) {
        fileName = tempUrl.substring(tempEnd + 1, tempUrl.length());
      }
    } else {
      int tempEnd = url.lastIndexOf("/");
      if (tempEnd > 0) {
        fileName = url.substring(tempEnd + 1, url.length());
      }
    }
    if (TextUtils.isEmpty(fileName)) {
      fileName = CommonUtil.keyToHashKey(url);
    }
    return fileName;
  }
}
