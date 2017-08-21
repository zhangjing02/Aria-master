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

import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.orm.Ignore;
import com.arialyy.aria.orm.Primary;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lyy on 2017/2/23.
 */
public abstract class AbsTaskEntity<ENTITY extends AbsEntity> extends DbEntity {
  /**
   * HTTP下载
   */
  public static final int HTTP = 0x11;
  /**
   * FTP当文件下载
   */
  public static final int FTP = 0x12;
  /**
   * FTP文件夹下载，为避免登录过多，子任务由单线程进行处理
   */
  public static final int FTP_DIR = 0x13;

  /**
   * Task实体对应的key
   */
  @Primary public String key = "";

  /**
   * 账号和密码
   */
  @Ignore public String userName, userPw, account, serverIp;
  @Ignore public int port;

  /**
   * 请求类型
   * {@link AbsTaskEntity#HTTP}、{@link AbsTaskEntity#FTP}
   */
  public int requestType = HTTP;

  /**
   * http 请求头
   */
  public Map<String, String> headers = new HashMap<>();

  /**
   * 字符编码，默认为"utf-8"
   */
  public String charSet = "utf-8";

  /**
   * 网络请求类型
   */
  public RequestEnum requestEnum = RequestEnum.GET;

  /**
   * 从header中含有的文件md5码信息所需要的key
   */
  public String md5Key = "Content-MD5";

  /**
   * 从header中获取文件描述信息所需要的key
   */
  public String dispositionKey = "Content-Disposition";

  /**
   * 重定向后，从header中获取新url所需要的key
   */
  public String redirectUrlKey = "location";

  /**
   * 从Disposition获取的文件名说需要的key
   */
  public String dispositionFileKey = "attachment;filename";

  /**
   * 从header中含有的文件长度信息所需要的key
   */
  public String contentLength = "Content-Length";

  /**
   * 重定向链接
   */
  public String redirectUrl = "";

  /**
   * {@code true}  删除任务数据库记录，并且删除已经下载完成的文件
   * {@code false} 如果任务已经完成，只删除任务数据库记录
   */
  @Ignore public boolean removeFile = false;

  /**
   * 是否支持断点, {@code true} 为支持断点
   */
  public boolean isSupportBP = true;

  /**
   * 状态码
   */
  public int code;

  public abstract ENTITY getEntity();

  /**
   * 获取任务下载状态
   *
   * @return {@link IEntity}
   */
  public int getState() {
    return getEntity().getState();
  }
}
