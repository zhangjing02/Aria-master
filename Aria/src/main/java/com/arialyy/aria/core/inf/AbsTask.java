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

import android.content.Context;
import android.os.Handler;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by AriaL on 2017/6/29.
 */
public abstract class AbsTask<ENTITY extends AbsEntity> implements ITask<ENTITY> {

  protected ENTITY mEntity;
  protected Handler mOutHandler;

  /**
   * 用于生成该任务对象的hash码
   */
  private String mTargetName;
  protected Context mContext;

  protected boolean isHeighestTask = false;

  /**
   * 任务是否完成
   *
   * @return {@code true} 已经完成，{@code false} 未完成
   */
  public boolean isComplete() {
    return mEntity.isComplete();
  }

  /**
   * 获取当前下载进度
   */
  @Override public long getCurrentProgress() {
    return mEntity.getCurrentProgress();
  }

  /**
   * 获取单位转换后的进度
   *
   * @return 如：已经下载3mb的大小，则返回{@code 3mb}
   */
  @Override public String getConvertCurrentProgress() {
    if (mEntity.getCurrentProgress() == 0) {
      return "0b";
    }
    return CommonUtil.formatFileSize(mEntity.getCurrentProgress());
  }

  /**
   * 转换单位后的文件长度
   *
   * @return 如果文件长度为0，则返回0m，否则返回转换后的长度1b、1kb、1mb、1gb、1tb
   */
  @Override public String getConvertFileSize() {
    if (mEntity.getFileSize() == 0) {
      return "0mb";
    }
    return CommonUtil.formatFileSize(mEntity.getFileSize());
  }

  /**
   * 获取文件大小
   */
  @Override public long getFileSize() {
    return mEntity.getFileSize();
  }

  /**
   * 获取百分比进度
   *
   * @return 返回百分比进度，如果文件长度为0，返回0
   */
  @Override public int getPercent() {
    if (mEntity.getFileSize() == 0) {
      return 0;
    }
    return (int) (mEntity.getCurrentProgress() * 100 / mEntity.getFileSize());
  }

  /**
   * 任务当前状态
   *
   * @return {@link IEntity}
   */
  public int getState() {
    return mEntity == null ? IEntity.STATE_OTHER : mEntity.getState();
  }

  /**
   * 获取保存的扩展字段
   *
   * @return 如果实体不存在，则返回null，否则返回扩展字段
   */
  @Override public String getExtendField() {
    return mEntity == null ? null : mEntity.getStr();
  }

  /**
   * @return 返回原始byte速度，需要你在配置文件中配置
   * <pre>
   *   {@code
   *    <xml>
   *      <download>
   *        ...
   *        <convertSpeed value="false"/>
   *      </download>
   *
   *      或在代码中设置
   *      Aria.get(this).getDownloadConfig().setConvertSpeed(false);
   *    </xml>
   *   }
   * </pre>
   * 才能生效
   */
  @Override public long getSpeed() {
    return mEntity.getSpeed();
  }

  /**
   * @return 返回转换单位后的速度，需要你在配置文件中配置，转换完成后为：1b/s、1kb/s、1mb/s、1gb/s、1tb/s
   * <pre>
   *   {@code
   *    <xml>
   *      <download>
   *        ...
   *        <convertSpeed value="true"/>
   *      </download>
   *
   *      或在代码中设置
   *      Aria.get(this).getDownloadConfig().setConvertSpeed(true);
   *    </xml>
   *   }
   * </pre>
   * 才能生效
   */
  @Override public String getConvertSpeed() {
    return mEntity.getConvertSpeed();
  }

  @Override public ENTITY getEntity() {
    return mEntity;
  }

  public String getTargetName() {
    return mTargetName;
  }

  @Override public void setTargetName(String targetName) {
    this.mTargetName = targetName;
  }

  public boolean isHighestPriorityTask() {
    return isHeighestTask;
  }
}
