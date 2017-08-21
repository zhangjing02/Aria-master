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
package com.arialyy.aria.orm;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.arialyy.aria.util.CommonUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Aria.Lao on 2017/7/24.
 * sql工具
 */
final class SqlUtil {

  /**
   * 获取一对一参数
   */
  static String getOneToOneParams(Field field) {
    OneToOne oneToOne = field.getAnnotation(OneToOne.class);
    if (oneToOne == null) {
      throw new IllegalArgumentException("@OneToOne注解的对象必须要有@Primary注解的字段");
    }
    return oneToOne.table().getName() + "$$" + oneToOne.key();
  }

  /**
   * 获取List一对多参数
   *
   * @param field list反射字段
   */
  static String getOneToManyElementParams(Field field) {
    OneToMany oneToMany = field.getAnnotation(OneToMany.class);
    if (oneToMany == null) {
      throw new IllegalArgumentException("一对多元素必须被@OneToMany注解");
    }
    //关联的表名
    String tableName = oneToMany.table().getName();
    //关联的字段
    String key = oneToMany.key();
    return tableName + "$$" + key;
  }

  /**
   * 列表数据转字符串
   *
   * @param field list反射字段
   */
  static String list2Str(DbEntity dbEntity, Field field) throws IllegalAccessException {
    NormalList normalList = field.getAnnotation(NormalList.class);
    if (normalList == null) {
      throw new IllegalArgumentException("List中元素必须被@NormalList注解");
    }
    List list = (List) field.get(dbEntity);
    if (list == null || list.isEmpty()) return "";
    StringBuilder sb = new StringBuilder();
    for (Object aList : list) {
      sb.append(aList).append("$$");
    }
    return sb.toString();
  }

  /**
   * 字符串转列表
   *
   * @param str 数据库中的字段
   * @return 如果str为null，则返回null
   */
  static List str2List(String str, Field field) {
    NormalList normalList = field.getAnnotation(NormalList.class);
    if (normalList == null) {
      throw new IllegalArgumentException("List中元素必须被@NormalList注解");
    }
    if (TextUtils.isEmpty(str)) return null;
    String[] datas = str.split("$$");
    List list = new ArrayList();
    String type = normalList.clazz().getName();
    for (String data : datas) {
      list.add(checkData(data, type));
    }
    return list;
  }

  /**
   * 字符串转Map，只支持
   * <pre>
   *   {@code Map<String, String>}
   * </pre>
   */
  static Map<String, String> str2Map(String str) {
    Map<String, String> map = new HashMap<>();
    if (TextUtils.isEmpty(str)) {
      return map;
    }
    String[] element = str.split(",");
    for (String data : element) {
      String[] s = data.split("\\$");
      map.put(s[0], s[1]);
    }
    return map;
  }

  /**
   * Map转字符串，只支持
   * <pre>
   *   {@code Map<String, String>}
   * </pre>
   */
  static String map2Str(Map<String, String> map) {
    StringBuilder sb = new StringBuilder();
    Set<String> keys = map.keySet();
    for (String key : keys) {
      sb.append(key).append("$").append(map.get(key)).append(",");
    }
    String str = sb.toString();
    return TextUtils.isEmpty(str) ? str : str.substring(0, str.length() - 1);
  }



  /**
   * @return true 忽略该字段
   */
  static boolean ignoreField(Field field) {
    // field.isSynthetic(), 使用as热启动App时，AS会自动给你的class添加change字段
    Ignore ignore = field.getAnnotation(Ignore.class);
    int modifiers = field.getModifiers();
    return (ignore != null && ignore.value())
        || field.getName().equals("rowID")
        || field.isSynthetic()
        || Modifier.isStatic(modifiers)
        || Modifier.isFinal(modifiers);
  }

  /**
   * 判断是否一对多注解
   */
  static boolean isOneToMany(Field field) {
    OneToMany oneToMany = field.getAnnotation(OneToMany.class);
    return oneToMany != null;
  }

  /**
   * 判断是否是一对一注解
   */
  static boolean isOneToOne(Field field) {
    OneToOne oneToOne = field.getAnnotation(OneToOne.class);
    return oneToOne != null;
  }

  /**
   * 判断是否是主键
   */
  static boolean isPrimary(Field field) {
    Primary pk = field.getAnnotation(Primary.class);
    return pk != null;
  }

  private static Object checkData(String type, String data) {
    switch (type) {
      case "String":
        return data;
      case "int":
      case "Integer":
        return Integer.parseInt(data);
      case "double":
      case "Double":
        return Double.parseDouble(data);
      case "float":
      case "Float":
        return Float.parseFloat(data);
    }
    return null;
  }


  /**
   * 查找class的主键字段
   *
   * @return 返回主键字段名
   */
  static String getPrimaryName(Class<? extends DbEntity> clazz) {
    List<Field> fields = CommonUtil.getAllFields(clazz);
    for (Field field : fields) {
      if (isPrimary(field)) return field.getName();
    }
    return null;
  }
}
