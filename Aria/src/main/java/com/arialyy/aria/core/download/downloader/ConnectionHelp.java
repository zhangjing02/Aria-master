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

import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.util.SSLContextUtil;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.net.ftp.FTPClient;

/**
 * Created by lyy on 2017/1/18.
 * 链接帮助类
 */
class ConnectionHelp {

  /**
   * 处理链接
   *
   * @throws IOException
   */
  static HttpURLConnection handleConnection(URL url) throws IOException {
    HttpURLConnection conn;
    URLConnection urlConn = url.openConnection();
    if (urlConn instanceof HttpsURLConnection) {
      conn = (HttpsURLConnection) urlConn;
      SSLContext sslContext =
          SSLContextUtil.getSSLContext(SSLContextUtil.CA_ALIAS, SSLContextUtil.CA_PATH);
      if (sslContext == null) {
        sslContext = SSLContextUtil.getDefaultSLLContext();
      }
      SSLSocketFactory ssf = sslContext.getSocketFactory();
      ((HttpsURLConnection) conn).setSSLSocketFactory(ssf);
      ((HttpsURLConnection) conn).setHostnameVerifier(SSLContextUtil.HOSTNAME_VERIFIER);
    } else {
      conn = (HttpURLConnection) urlConn;
    }
    return conn;
  }

  /**
   * 设置头部参数
   *
   * @throws ProtocolException
   */
  static HttpURLConnection setConnectParam(DownloadTaskEntity entity, HttpURLConnection conn)
      throws ProtocolException {
    conn.setRequestMethod(entity.requestEnum.name);
    Set<String> keys = null;
    if (entity.headers != null && entity.headers.size() > 0) {
      keys = entity.headers.keySet();
      for (String key : keys) {
        conn.setRequestProperty(key, entity.headers.get(key));
      }
    }
    if (keys == null || !keys.contains("Charset")) {
      conn.setRequestProperty("Charset", "UTF-8");
    }
    if (keys == null || !keys.contains("User-Agent")) {
      conn.setRequestProperty("User-Agent",
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
    }
    if (keys == null || !keys.contains("Accept")) {
      StringBuilder accept = new StringBuilder();
      accept.append("image/gif, ")
          .append("image/jpeg, ")
          .append("image/pjpeg, ")
          .append("image/webp, ")
          .append("application/xml, ")
          .append("application/xaml+xml, ")
          .append("application/xhtml+xml, ")
          .append("application/x-shockwave-flash, ")
          .append("application/x-ms-xbap, ")
          .append("application/x-ms-application, ")
          .append("application/msword, ")
          .append("application/vnd.ms-excel, ")
          .append("application/vnd.ms-xpsdocument, ")
          .append("application/vnd.ms-powerpoint, ")
          .append("text/plain, ")
          .append("text/html, ")
          .append("*/*");
      conn.setRequestProperty("Accept", accept.toString());
    }
    if (keys == null || !keys.contains("Accept-Encoding")) {
      conn.setRequestProperty("Accept-Encoding", "identity");
    }
    if (keys == null || !keys.contains("Accept-Charset")) {
      conn.setRequestProperty("Accept-Charset", "UTF-8");
    }
    if (keys == null || !keys.contains("Connection")) {
      conn.setRequestProperty("Connection", "Keep-Alive");
    }
    //302获取重定向地址
    conn.setInstanceFollowRedirects(false);
    return conn;
  }
}
