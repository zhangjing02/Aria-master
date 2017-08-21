package com.arialyy.aria.core.common;

import com.arialyy.aria.core.inf.AbsTaskEntity;
import java.io.File;

/**
 * 子线程下载信息类
 */
public class SubThreadConfig<TASK_ENTITY extends AbsTaskEntity> {
  //线程Id
  public int THREAD_ID;
  //下载文件大小
  public long FILE_SIZE;
  //子线程启动下载位置
  public long START_LOCATION;
  //子线程结束下载位置
  public long END_LOCATION;
  //下载文件或上传的文件路径
  public File TEMP_FILE;
  //服务器地址
  public String URL;
  public String CONFIG_FILE_PATH;
  public TASK_ENTITY TASK_ENTITY;
  public boolean SUPPORT_BP = true;
}