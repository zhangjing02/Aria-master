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

import android.support.annotation.NonNull;
import com.arialyy.aria.util.CommonUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lyy on 2015/11/2.
 * 所有数据库实体父类
 */
public class DbEntity {
  private static final Object LOCK = new Object();
  protected int rowID = -1;

  protected DbEntity() {

  }

  /**
   * 直接执行sql语句
   */
  public static void exeSql(String sql) {
    DbUtil.getInstance().exeSql(sql);
  }

  /**
   * 查询所有数据
   *
   * @return 没有数据返回null
   */
  public static <T extends DbEntity> List<T> findAllData(Class<T> clazz) {
    DbUtil util = DbUtil.getInstance();
    return util.findAllData(clazz);
  }

  /**
   * 查询第一条数据
   */
  public static <T extends DbEntity> T findFirst(Class<T> clazz) {
    List<T> list = findAllData(clazz);
    return (list == null || list.size() == 0) ? null : list.get(0);
  }

  /**
   * 查询一组数据
   * <code>
   * DownloadEntity.findFirst(DownloadEntity.class, "downloadUrl=?", downloadUrl);
   * </code>
   *
   * @return 没有数据返回null
   */
  public static <T extends DbEntity> List<T> findDatas(Class<T> clazz, String... expression) {
    DbUtil util = DbUtil.getInstance();
    return util.findData(clazz, expression);
  }

  /**
   * 查询一行数据
   * <code>
   * DownloadEntity.findFirst(DownloadEntity.class, "downloadUrl=?", downloadUrl);
   * </code>
   *
   * @return 没有数据返回null
   */
  public static <T extends DbEntity> T findFirst(Class<T> clazz, String... expression) {
    DbUtil util = DbUtil.getInstance();
    List<T> datas = util.findData(clazz, expression);
    return datas == null ? null : datas.size() > 0 ? datas.get(0) : null;
  }

  /**
   * 获取所有行的rowid
   */
  public int[] getRowIds() {
    return DbUtil.getInstance().getRowId(getClass());
  }

  /**
   * 获取rowid
   */
  public int getRowId(@NonNull Object[] wheres, @NonNull Object[] values) {
    return DbUtil.getInstance().getRowId(getClass(), wheres, values);
  }

  /**
   * 删除当前数据
   */
  public void deleteData() {
    deleteData(getClass(), "rowid=?", rowID + "");
  }

  /**
   * 根据条件删除数据
   * <code>
   * DownloadEntity.deleteData(DownloadEntity.class, "downloadUrl=?", downloadUrl);
   * </code>
   */
  public static <T extends DbEntity> void deleteData(Class<T> clazz, String... expression) {
    DbUtil util = DbUtil.getInstance();
    util.delData(clazz, expression);
  }

  /**
   * 修改数据
   */
  public void update() {
    DbUtil.getInstance().modifyData(this);
  }

  /**
   * 保存自身，如果表中已经有数据，则更新数据，否则插入数据
   */
  public void save() {
    synchronized (LOCK) {
      if (thisIsExist()) {
        update();
      } else {
        insert();
      }
    }
  }

  /**
   * 查找数据在表中是否存在
   */
  private boolean thisIsExist() {
    DbUtil util = DbUtil.getInstance();
    return util.isExist(getClass(), rowID);
  }

  /**
   * 插入数据
   */
  public void insert() {
    DbUtil.getInstance().insertData(this);
    updateRowID();
  }

  private <T extends DbEntity> T findFirst(Class<T> clazz, @NonNull String[] wheres,
      @NonNull String[] values) {
    DbUtil util = DbUtil.getInstance();
    List<T> list = util.findData(clazz, wheres, values);
    return list == null ? null : list.get(0);
  }

  private void updateRowID() {
    try {
      List<Field> fields = CommonUtil.getAllFields(getClass());
      List<String> where = new ArrayList<>();
      List<String> values = new ArrayList<>();
      for (Field field : fields) {
        field.setAccessible(true);
        if (SqlUtil.ignoreField(field)) {
          continue;
        }
        where.add(field.getName());
        Type type = field.getType();
        if (SqlUtil.isOneToOne(field)) {
          values.add(SqlUtil.getOneToOneParams(field));
        } else if (type == List.class) {
          if (SqlUtil.isOneToMany(field)) {
            values.add(SqlUtil.getOneToManyElementParams(field));
          } else {
            values.add(SqlUtil.list2Str(this, field));
          }
        } else if (type == Map.class) {
          values.add(SqlUtil.map2Str((Map<String, String>) field.get(this)));
        } else {
          values.add(field.get(this) + "");
        }
      }
      DbEntity entity = findFirst(getClass(), where.toArray(new String[where.size()]),
          values.toArray(new String[values.size()]));
      if (entity != null) {
        rowID = entity.rowID;
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}