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

import android.util.Log;
import com.arialyy.aria.core.common.AbsThreadTask;
import com.arialyy.aria.core.common.StateConstance;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.IDownloadListener;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by lyy on 2017/1/18.
 * 下载线程
 */
final class HttpThreadTask extends AbsThreadTask<DownloadEntity, DownloadTaskEntity> {
  private final String TAG = "HttpThreadTask";

  HttpThreadTask(StateConstance constance, IDownloadListener listener,
      SubThreadConfig<DownloadTaskEntity> downloadInfo) {
    super(constance, listener, downloadInfo);
  }

  @Override public void run() {
    HttpURLConnection conn = null;
    InputStream is = null;
    BufferedRandomAccessFile file = null;
    try {
      URL url = new URL(CommonUtil.convertUrl(mConfig.URL));
      conn = ConnectionHelp.handleConnection(url);
      if (mConfig.SUPPORT_BP) {
        Log.d(TAG, "任务【"
            + mConfig.TEMP_FILE.getName()
            + "】线程__"
            + mConfig.THREAD_ID
            + "__开始下载【开始位置 : "
            + mConfig.START_LOCATION
            + "，结束位置："
            + mConfig.END_LOCATION
            + "】");
        //在头里面请求下载开始位置和结束位置
        conn.setRequestProperty("Range",
            "bytes=" + mConfig.START_LOCATION + "-" + (mConfig.END_LOCATION - 1));
      } else {
        Log.w(TAG, "该下载不支持断点");
      }
      conn = ConnectionHelp.setConnectParam(mConfig.TASK_ENTITY, conn);
      conn.setConnectTimeout(STATE.CONNECT_TIME_OUT);
      conn.setReadTimeout(STATE.READ_TIME_OUT);  //设置读取流的等待时间,必须设置该参数
      is = conn.getInputStream();
      //创建可设置位置的文件
      file = new BufferedRandomAccessFile(mConfig.TEMP_FILE, "rwd", mBufSize);
      //设置每条线程写入文件的位置
      file.seek(mConfig.START_LOCATION);
      byte[] buffer = new byte[mBufSize];
      int len;
      //当前子线程的下载位置
      mChildCurrentLocation = mConfig.START_LOCATION;
      while ((len = is.read(buffer)) != -1) {
        if (STATE.isCancel) break;
        if (STATE.isStop) break;
        if (mSleepTime > 0) Thread.sleep(mSleepTime);
        file.write(buffer, 0, len);
        progress(len);
      }
      if (STATE.isCancel || STATE.isStop) return;
      //支持断点的处理
      if (mConfig.SUPPORT_BP) {
        Log.i(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】线程__" + mConfig.THREAD_ID + "__下载完毕");
        writeConfig(true, 1);
        STATE.COMPLETE_THREAD_NUM++;
        if (STATE.isComplete()) {
          File configFile = new File(mConfigFPath);
          if (configFile.exists()) {
            configFile.delete();
          }
          STATE.isRunning = false;
          mListener.onComplete();
        }
      } else {
        Log.i(TAG, "下载任务完成");
        STATE.isRunning = false;
        mListener.onComplete();
      }
    } catch (MalformedURLException e) {
      fail(mChildCurrentLocation, "下载链接异常", e);
    } catch (IOException e) {
      fail(mChildCurrentLocation, "下载失败【" + mConfig.URL + "】", e);
    } catch (Exception e) {
      fail(mChildCurrentLocation, "获取流失败", e);
    } finally {
      try {
        if (file != null) {
          file.close();
        }
        if (is != null) {
          is.close();
        }
        if (conn != null) {
          conn.disconnect();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
