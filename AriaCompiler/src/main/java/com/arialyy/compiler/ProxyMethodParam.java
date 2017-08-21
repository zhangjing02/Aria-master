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
package com.arialyy.compiler;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Aria.Lao on 2017/6/7.
 * 创建代理方法的参数
 */
class ProxyMethodParam {
  String packageName;
  String className;
  Set<TaskEnum> taskEnums;
  Map<String, Set<String>> keyMappings = new HashMap<>();
  Map<TaskEnum, Map<Class<? extends Annotation>, String>> methods = new HashMap<>();
}
