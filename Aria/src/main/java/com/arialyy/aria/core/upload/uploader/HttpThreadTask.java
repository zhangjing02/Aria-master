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
package com.arialyy.aria.core.upload.uploader;

import android.util.Log;
import com.arialyy.aria.core.common.AbsThreadTask;
import com.arialyy.aria.core.common.StateConstance;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.inf.IUploadListener;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Aria.Lao on 2017/7/28.
 * 不支持断点的HTTP上传任务
 */
class HttpThreadTask extends AbsThreadTask<UploadEntity, UploadTaskEntity> {
  private final String TAG = "HttpThreadTask";

  private final String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
  private final String PREFIX = "--", LINE_END = "\r\n";
  private HttpURLConnection mHttpConn;
  private long mCurrentLocation = 0;
  private OutputStream mOutputStream;

  HttpThreadTask(StateConstance constance, IUploadListener listener,
      SubThreadConfig<UploadTaskEntity> uploadInfo) {
    super(constance, listener, uploadInfo);
  }

  @Override public void run() {
    File uploadFile = new File(mEntity.getFilePath());
    if (!uploadFile.exists()) {
      Log.e(TAG, "【" + mEntity.getFilePath() + "】，文件不存在。");
      fail();
      return;
    }
    mListener.onPre();
    URL url;
    try {
      url = new URL(mEntity.getUrl());
      mHttpConn = (HttpURLConnection) url.openConnection();
      mHttpConn.setUseCaches(false);
      mHttpConn.setDoOutput(true);
      mHttpConn.setDoInput(true);
      mHttpConn.setRequestProperty("Content-Type",
          mTaskEntity.contentType + "; boundary=" + BOUNDARY);
      mHttpConn.setRequestProperty("User-Agent", mTaskEntity.userAgent);
      //mHttpConn.setRequestProperty("Range", "bytes=" + 0 + "-" + "100");
      //内部缓冲区---分段上传防止oom
      mHttpConn.setChunkedStreamingMode(1024);

      //添加Http请求头部
      Set<String> keys = mTaskEntity.headers.keySet();
      for (String key : keys) {
        mHttpConn.setRequestProperty(key, mTaskEntity.headers.get(key));
      }
      mOutputStream = mHttpConn.getOutputStream();
      PrintWriter writer =
          new PrintWriter(new OutputStreamWriter(mOutputStream, mTaskEntity.charSet), true);
      //添加文件上传表单字段
      keys = mTaskEntity.formFields.keySet();
      for (String key : keys) {
        addFormField(writer, key, mTaskEntity.formFields.get(key));
      }
      uploadFile(writer, mTaskEntity.attachment, uploadFile);
      Log.d(TAG, finish(writer) + "");
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
  }

  private void fail() {
    try {
      mListener.onFail();
      STATE.isRunning = false;
      if (mOutputStream != null) {
        mOutputStream.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 添加文件上传表单字段
   */
  private void addFormField(PrintWriter writer, String name, String value) {
    writer.append(PREFIX).append(BOUNDARY).append(LINE_END);
    writer.append("Content-Disposition: form-data; name=\"")
        .append(name)
        .append("\"")
        .append(LINE_END);
    writer.append("Content-Type: text/plain; charset=")
        .append(mTaskEntity.charSet)
        .append(LINE_END);
    writer.append(LINE_END);
    writer.append(value).append(LINE_END);
    writer.flush();
  }

  /**
   * 上传文件
   *
   * @param attachment 文件上传attachment
   * @throws IOException
   */
  private void uploadFile(PrintWriter writer, String attachment, File uploadFile)
      throws IOException {
    writer.append(PREFIX).append(BOUNDARY).append(LINE_END);
    writer.append("Content-Disposition: form-data; name=\"")
        .append(attachment)
        .append("\"; filename=\"")
        .append(mTaskEntity.getEntity().getFileName())
        .append("\"")
        .append(LINE_END);
    writer.append("Content-Type: ")
        .append(URLConnection.guessContentTypeFromName(mTaskEntity.getEntity().getFileName()))
        .append(LINE_END);
    writer.append("Content-Transfer-Encoding: binary").append(LINE_END);
    writer.append(LINE_END);
    writer.flush();

    FileInputStream inputStream = new FileInputStream(uploadFile);
    byte[] buffer = new byte[4096];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      mCurrentLocation += bytesRead;
      mOutputStream.write(buffer, 0, bytesRead);
      if (STATE.isCancel) {
        break;
      }
      mListener.onProgress(mCurrentLocation);
    }

    mOutputStream.flush();
    //outputStream.close(); //不能调用，否则服务器端异常
    inputStream.close();
    writer.append(LINE_END);
    writer.flush();
    if (STATE.isCancel) {
      mListener.onCancel();
      STATE.isRunning = false;
      return;
    }
    mListener.onComplete();
    STATE.isRunning = false;
  }

  /**
   * 任务结束操作
   *
   * @throws IOException
   */
  private String finish(PrintWriter writer) throws IOException {
    StringBuilder response = new StringBuilder();

    writer.append(LINE_END).flush();
    writer.append(PREFIX).append(BOUNDARY).append(PREFIX).append(LINE_END);
    writer.close();

    int status = mHttpConn.getResponseCode();
    if (status == HttpURLConnection.HTTP_OK) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(mHttpConn.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();
      mHttpConn.disconnect();
    } else {
      Log.w(TAG, "state_code = " + status);
      fail();
    }

    writer.flush();
    writer.close();
    mOutputStream.close();
    return response.toString();
  }
}
