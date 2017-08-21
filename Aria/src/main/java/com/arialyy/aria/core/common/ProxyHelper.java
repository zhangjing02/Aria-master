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
package com.arialyy.aria.core.common;

import com.arialyy.aria.core.AriaManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * Created by Aria.Lao on 2017/7/10.
 * 代理参数获取
 */
public class ProxyHelper {
  public Set<String> downloadCounter, uploadCounter, downloadGroupCounter;

  public static volatile ProxyHelper INSTANCE = null;

  private ProxyHelper() {
    init();
  }

  public static ProxyHelper getInstance() {
    if (INSTANCE == null) {
      synchronized (AriaManager.LOCK) {
        INSTANCE = new ProxyHelper();
      }
    }
    return INSTANCE;
  }

  private void init() {
    try {
      Class clazz = Class.forName("com.arialyy.aria.ProxyClassCounter");
      Method download = clazz.getMethod("getDownloadCounter");
      Method downloadGroup = clazz.getMethod("getDownloadGroupCounter");
      Method upload = clazz.getMethod("getUploadCounter");
      Object object = clazz.newInstance();
      Object dc = download.invoke(object);
      if (dc != null) {
        downloadCounter = unmodifiableSet((Set<String>) dc);
      }
      Object dgc = downloadGroup.invoke(object);
      if (dgc != null) {
        downloadGroupCounter = unmodifiableSet((Set<String>) dgc);
      }
      Object uc = upload.invoke(object);
      if (uc != null) {
        uploadCounter = unmodifiableSet((Set<String>) uc);
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}
