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
package com.arialyy.aria.core;

import android.text.TextUtils;
import android.util.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by lyy on 2017/5/22.
 * 读取配置文件
 */
class ConfigHelper extends DefaultHandler {
  private final String TAG = "ConfigHelper";

  private boolean isDownloadConfig = false, isUploadConfig;
  private Configuration.DownloadConfig mDownloadConfig = Configuration.DownloadConfig.getInstance();
  private Configuration.UploadConfig mUploadConfig = Configuration.UploadConfig.getInstance();

  @Override public void startDocument() throws SAXException {
    super.startDocument();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    super.startElement(uri, localName, qName, attributes);
    if (qName.equals("download")) {
      isDownloadConfig = true;
      isUploadConfig = false;
    } else if (qName.equals("upload")) {
      isUploadConfig = true;
      isDownloadConfig = false;
    }
    if (isDownloadConfig || isUploadConfig) {

      String value = attributes.getValue("value");
      switch (qName) {
        case "threadNum":
          loadThreadNum(value);
          break;
        case "maxTaskNum":
          loadMaxQueue(value);
          break;
        case "reTryNum":
          loadReTry(value);
          break;
        case "connectTimeOut":
          loadConnectTime(value);
          break;
        case "iOTimeOut":
          loadIOTimeout(value);
          break;
        case "reTryInterval":
          loadReTryInterval(value);
          break;
        case "buffSize":
          loadBuffSize(value);
          break;
        case "ca":
          String caName = attributes.getValue("name");
          String caPath = attributes.getValue("path");
          loadCA(caName, caPath);
          break;
        case "convertSpeed":
          loadConvertSpeed(value);
          break;
        case "maxSpeed":
          loadMaxSpeed(value);
          break;
        case "queueMod":
          loadQueueMod(value);
          break;
      }
    }
  }

  private void loadQueueMod(String value) {
    String mod = "now";
    if (!TextUtils.isEmpty(value) && (value.equalsIgnoreCase("now") || value.equalsIgnoreCase(
        "wait"))) {
      mod = value;
    }
    if (isDownloadConfig) {
      mDownloadConfig.queueMod = mod;
    }
    if (isUploadConfig) {
      mUploadConfig.queueMod = mod;
    }
  }

  private void loadMaxSpeed(String value) {
    double maxSpeed = 0.0;
    if (!TextUtils.isEmpty(value)) {
      maxSpeed = Double.parseDouble(value);
    }
    if (isDownloadConfig) {
      mDownloadConfig.msxSpeed = maxSpeed;
    }
  }

  private void loadConvertSpeed(String value) {
    boolean open = Boolean.parseBoolean(value);
    if (isDownloadConfig) {
      mDownloadConfig.isConvertSpeed = open;
    }
    if (isUploadConfig) {
      mUploadConfig.isConvertSpeed = open;
    }
  }

  private void loadReTryInterval(String value) {
    int time = 2 * 1000;
    if (!TextUtils.isEmpty(value)) {
      time = Integer.parseInt(value);
    }

    if (time < 2 * 1000) {
      time = 2 * 1000;
    }

    if (isDownloadConfig) {
      mDownloadConfig.reTryInterval = time;
    }
  }

  private void loadCA(String name, String path) {
    if (isDownloadConfig) {
      mDownloadConfig.caName = name;
      mDownloadConfig.caPath = path;
    }
  }

  private void loadBuffSize(String value) {
    int buffSize = 8192;
    if (!TextUtils.isEmpty(value)) {
      buffSize = Integer.parseInt(value);
    }

    if (buffSize < 2048) {
      buffSize = 2048;
    }

    if (isDownloadConfig) {
      mDownloadConfig.buffSize = buffSize;
    }
  }

  private void loadIOTimeout(String value) {
    int time = 10 * 1000;
    if (!TextUtils.isEmpty(value)) {
      time = Integer.parseInt(value);
    }

    if (time < 10 * 1000) {
      time = 10 * 1000;
    }

    if (isDownloadConfig) {
      mDownloadConfig.iOTimeOut = time;
    }
  }

  private void loadConnectTime(String value) {
    int time = 5 * 1000;
    if (!TextUtils.isEmpty(value)) {
      time = Integer.parseInt(value);
    }

    if (isDownloadConfig) {
      mDownloadConfig.connectTimeOut = time;
    }
    if (isUploadConfig) {
      mUploadConfig.connectTimeOut = time;
    }
  }

  private void loadReTry(String value) {
    int num = 0;
    if (!TextUtils.isEmpty(value)) {
      num = Integer.parseInt(value);
    }

    if (isDownloadConfig) {
      mDownloadConfig.reTryNum = num;
    }
    if (isUploadConfig) {
      mUploadConfig.reTryNum = num;
    }
  }

  private void loadMaxQueue(String value) {
    int num = 2;
    if (!TextUtils.isEmpty(value)) {
      num = Integer.parseInt(value);
    }
    if (num < 1) {
      Log.e(TAG, "任务队列数不能小于 1");
      num = 2;
    }
    if (isDownloadConfig) {
      mDownloadConfig.maxTaskNum = num;
    }
    if (isUploadConfig) {
      mUploadConfig.maxTaskNum = num;
    }
  }

  private void loadThreadNum(String value) {
    int num = 3;
    if (!TextUtils.isEmpty(value)) {
      num = Integer.parseInt(value);
    }
    if (num < 1) {
      Log.e(TAG, "下载线程数不能小于 1");
      num = 1;
    }
    if (isDownloadConfig) {
      mDownloadConfig.threadNum = num;
    }
  }

  @Override public void characters(char[] ch, int start, int length) throws SAXException {
    super.characters(ch, start, length);
  }

  @Override public void endElement(String uri, String localName, String qName) throws SAXException {
    super.endElement(uri, localName, qName);
  }

  @Override public void endDocument() throws SAXException {
    super.endDocument();
    mDownloadConfig.saveAll();
    mUploadConfig.saveAll();
  }
}
