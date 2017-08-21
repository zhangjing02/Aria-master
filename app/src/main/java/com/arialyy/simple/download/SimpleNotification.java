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

import android.app.NotificationManager;
import android.content.Context;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.simple.R;

/**
 * Created by Aria.Lao on 2017/1/18.
 */

public class SimpleNotification {
  private static final String DOWNLOAD_URL =
      "http://static.gaoshouyou.com/d/cb/38/f0cb1b2c57388fe14342eecd64bbae65.apk";

  private NotificationManager mManager;
  private Context mContext;
  private NotificationCompat.Builder mBuilder;
  private static final int mNotifiyId = 0;

  public SimpleNotification(Context context) {
    mContext = context;
    init();
  }

  private void init() {
    mManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    mBuilder = new NotificationCompat.Builder(mContext);
    mBuilder.setContentTitle("Aria Download Test")
        .setContentText("进度条")
        .setProgress(100, 0, false)
        .setSmallIcon(R.mipmap.ic_launcher);
    mManager.notify(mNotifiyId, mBuilder.build());
    Aria.download(mContext).addSchedulerListener(new DownloadCallback(mBuilder, mManager));
  }

  public void start() {
    Aria.download(mContext)
        .load(DOWNLOAD_URL)
        .setDownloadPath(
            Environment.getExternalStorageDirectory() + "/Download/消灭星星.apk")
        .start();
  }

  public void stop() {
    Aria.download(mContext).load(DOWNLOAD_URL).pause();
  }

  private static class DownloadCallback extends Aria.DownloadSchedulerListener {
    NotificationCompat.Builder mBuilder;
    NotificationManager mManager;

    private DownloadCallback(NotificationCompat.Builder builder, NotificationManager manager) {
      mBuilder = builder;
      mManager = manager;
    }

    @Override public void onTaskStart(DownloadTask task) {
      super.onTaskStart(task);
    }

    @Override public void onTaskPre(DownloadTask task) {
      super.onTaskPre(task);
    }

    @Override public void onTaskStop(DownloadTask task) {
      super.onTaskStop(task);
    }

    @Override public void onTaskRunning(DownloadTask task) {
      super.onTaskRunning(task);
      long len = task.getFileSize();
      int p = (int) (task.getCurrentProgress() * 100 / len);
      if (mBuilder != null) {
        mBuilder.setProgress(100, p, false);
        mManager.notify(mNotifiyId, mBuilder.build());
      }
    }

    @Override public void onTaskComplete(DownloadTask task) {
      super.onTaskComplete(task);
      if (mBuilder != null) {
        mBuilder.setProgress(100, 100, false);
        mManager.notify(mNotifiyId, mBuilder.build());
      }
    }

    @Override public void onTaskCancel(DownloadTask task) {
      super.onTaskCancel(task);
    }
  }
}
