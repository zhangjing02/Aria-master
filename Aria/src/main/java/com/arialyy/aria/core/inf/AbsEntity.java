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

import android.os.Parcel;
import android.os.Parcelable;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.orm.Ignore;

/**
 * Created by AriaL on 2017/6/29.
 */
public abstract class AbsEntity extends DbEntity implements IEntity, Parcelable {
  /**
   * 速度
   */
  @Ignore private long speed = 0;
  /**
   * 单位转换后的速度
   */
  @Ignore private String convertSpeed = "";
  /**
   * 下载失败计数，每次开始都重置为0
   */
  @Ignore private int failNum = 0;

  /**
   * 扩展字段
   */
  private String str = "";
  /**
   * 文件大小
   */
  private long fileSize = 1;
  /**
   * 转换后的文件大小
   */
  private String convertFileSize = "";

  private int state = STATE_WAIT;
  /**
   * 当前下载进度
   */
  private long currentProgress = 0;
  /**
   * 完成时间
   */
  private long completeTime;

  private boolean isComplete = false;

  public boolean isComplete() {
    return isComplete;
  }

  public void setComplete(boolean complete) {
    isComplete = complete;
  }

  public String getConvertFileSize() {
    return convertFileSize;
  }

  public void setConvertFileSize(String convertFileSize) {
    this.convertFileSize = convertFileSize;
  }

  public int getFailNum() {
    return failNum;
  }

  public void setFailNum(int failNum) {
    this.failNum = failNum;
  }

  public long getSpeed() {
    return speed;
  }

  public void setSpeed(long speed) {
    this.speed = speed;
  }

  public String getConvertSpeed() {
    return convertSpeed;
  }

  public void setConvertSpeed(String convertSpeed) {
    this.convertSpeed = convertSpeed;
  }

  public String getStr() {
    return str;
  }

  public void setStr(String str) {
    this.str = str;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public long getCurrentProgress() {
    return currentProgress;
  }

  public void setCurrentProgress(long currentProgress) {
    this.currentProgress = currentProgress;
  }

  public long getCompleteTime() {
    return completeTime;
  }

  public void setCompleteTime(long completeTime) {
    this.completeTime = completeTime;
  }

  /**
   * 实体唯一标识符
   */
  public abstract String getKey();

  public AbsEntity() {
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(this.speed);
    dest.writeString(this.convertSpeed);
    dest.writeInt(this.failNum);
    dest.writeString(this.str);
    dest.writeLong(this.fileSize);
    dest.writeString(this.convertFileSize);
    dest.writeInt(this.state);
    dest.writeLong(this.currentProgress);
    dest.writeLong(this.completeTime);
    dest.writeByte(this.isComplete ? (byte) 1 : (byte) 0);
  }

  protected AbsEntity(Parcel in) {
    this.speed = in.readLong();
    this.convertSpeed = in.readString();
    this.failNum = in.readInt();
    this.str = in.readString();
    this.fileSize = in.readLong();
    this.convertFileSize = in.readString();
    this.state = in.readInt();
    this.currentProgress = in.readLong();
    this.completeTime = in.readLong();
    this.isComplete = in.readByte() != 0;
  }
}
