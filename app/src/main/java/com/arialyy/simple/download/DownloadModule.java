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

package com.arialyy.simple.download;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.simple.R;
import com.arialyy.simple.download.multi_download.FileListEntity;
import com.arialyy.simple.base.BaseModule;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lyy on 2016/9/27.
 */
public class DownloadModule extends BaseModule {

  public DownloadModule(Context context) {
    super(context);
  }

  /**
   * 最高优先级任务测试列表
   */
  public List<DownloadEntity> getHighestTestList() {
    List<DownloadEntity> list = new LinkedList<>();
    Resources res = getContext().getResources();
    String[] urls = res.getStringArray(R.array.highest_urls);
    String[] names = res.getStringArray(R.array.highest_names);
    for (int i = 0, len = urls.length; i < len; i++) {
      list.add(createDownloadEntity(urls[i], names[i]));
    }
    return list;
  }

  /**
   * 创建下载地址
   */
  public List<FileListEntity> createMultiTestList() {
    String[] names = getContext().getResources().getStringArray(R.array.file_nams);
    String[] downloadUrl = getContext().getResources().getStringArray(R.array.download_url);
    List<FileListEntity> list = new ArrayList<>();
    int i = 0;
    for (String name : names) {
      FileListEntity entity = new FileListEntity();
      entity.name = name;
      entity.downloadUrl = downloadUrl[i];
      entity.downloadPath = Environment.getExternalStorageDirectory() + "/Download/" + name;
      list.add(entity);
      i++;
    }
    return list;
  }

  /**
   * 创建下载实体，Aria也可以通过下载实体启动下载
   */
  private DownloadEntity createDownloadEntity(String downloadUrl, String name) {
    String path = Environment.getExternalStorageDirectory() + "/download/" + name + ".apk";
    DownloadEntity entity = new DownloadEntity();
    entity.setFileName(name);
    entity.setUrl(downloadUrl);
    entity.setDownloadPath(path);
    return entity;
  }

}