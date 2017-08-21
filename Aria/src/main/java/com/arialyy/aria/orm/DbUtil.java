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

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import java.util.List;

/**
 * Created by lyy on 2015/2/11.
 * 数据库操作工具
 */
public class DbUtil {
  private static final String TAG = "DbUtil";
  private volatile static DbUtil INSTANCE = null;
  private int ROW_ID = 7;
  private SQLiteDatabase mDb;
  private SqlHelper mHelper;

  private DbUtil() {

  }

  private DbUtil(Context context) {
    mHelper = SqlHelper.init(context.getApplicationContext());
  }

  public static DbUtil init(Context context) {
    if (context instanceof Application) {
      synchronized (AriaManager.LOCK) {
        if (INSTANCE == null) {
          INSTANCE = new DbUtil(context);
        }
      }
    }
    return INSTANCE;
  }

  protected static DbUtil getInstance() {
    if (INSTANCE == null) {
      throw new NullPointerException("请在Application中调用init进行数据库工具注册注册");
    }
    return INSTANCE;
  }

  /**
   * 执行sql语句
   */
  void exeSql(String sql) {
    mDb.execSQL(sql);
  }

  /**
   * 删除某条数据
   */
  synchronized <T extends DbEntity> void delData(Class<T> clazz, String... expression) {
    CheckUtil.checkSqlExpression(expression);
    checkDb();
    SqlHelper.delData(mDb, clazz, expression);
  }

  /**
   * 修改某行数据
   */
  synchronized void modifyData(DbEntity dbEntity) {
    checkDb();
    SqlHelper.modifyData(mDb, dbEntity);
  }

  /**
   * 遍历所有数据
   */
  synchronized <T extends DbEntity> List<T> findAllData(Class<T> clazz) {
    checkDb();
    return SqlHelper.findAllData(mDb, clazz);
  }

  /**
   * 条件查寻数据
   */
  synchronized <T extends DbEntity> List<T> findData(Class<T> clazz, String... expression) {
    checkDb();
    return SqlHelper.findData(mDb, clazz, expression);
  }

  /**
   * 通过rowId判断数据是否存在
   */
  synchronized <T extends DbEntity> boolean isExist(Class<T> clazz, int rowId) {
    checkDb();
    String sql = "SELECT rowid FROM " + CommonUtil.getClassName(clazz) + " WHERE rowid=" + rowId;
    Cursor cursor = mDb.rawQuery(sql, null);
    boolean isExist = cursor.getCount() > 0;
    cursor.close();
    return isExist;
  }

  /**
   * 条件查寻数据
   */
  @Deprecated synchronized <T extends DbEntity> List<T> findData(Class<T> clazz,
      @NonNull String[] wheres, @NonNull String[] values) {
    checkDb();
    return SqlHelper.findData(mDb, clazz, wheres, values);
  }

  /**
   * 插入数据
   */
  synchronized void insertData(DbEntity dbEntity) {
    checkDb();
    SqlHelper.insertData(mDb, dbEntity);
  }

  /**
   * 查找某张表是否存在
   */
  synchronized boolean tableExists(Class clazz) {
    checkDb();
    return SqlHelper.tableExists(mDb, clazz);
  }

  synchronized void createTable(Class clazz, String tableName) {
    checkDb();
    SqlHelper.createTable(mDb, clazz, tableName);
  }

  private void checkDb() {
    if (mDb == null || !mDb.isOpen()) {
      mDb = mHelper.getReadableDatabase();
    }
  }

  /**
   * 创建表
   */
  private synchronized void createTable(Class clazz) {
    createTable(clazz, null);
  }

  /**
   * 关闭数据库
   */
  private synchronized void close() {
    if (mDb != null) {
      mDb.close();
    }
  }

  /**
   * 获取所在行Id
   */
  synchronized int[] getRowId(Class clazz) {
    checkDb();
    Cursor cursor = mDb.rawQuery("SELECT rowid, * FROM " + CommonUtil.getClassName(clazz), null);
    int[] ids = new int[cursor.getCount()];
    int i = 0;
    while (cursor.moveToNext()) {
      ids[i] = cursor.getInt(cursor.getColumnIndex("rowid"));
      i++;
    }
    cursor.close();
    close();
    return ids;
  }

  /**
   * 获取行Id
   */
  synchronized int getRowId(Class clazz, Object[] wheres, Object[] values) {
    checkDb();
    if (wheres.length <= 0 || values.length <= 0) {
      Log.e(TAG, "请输入删除条件");
      return -1;
    } else if (wheres.length != values.length) {
      Log.e(TAG, "groupName 和 vaule 长度不相等");
      return -1;
    }
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT rowid FROM ").append(CommonUtil.getClassName(clazz)).append(" WHERE ");
    int i = 0;
    for (Object where : wheres) {
      sb.append(where).append("=").append("'").append(values[i]).append("'");
      sb.append(i >= wheres.length - 1 ? "" : ",");
      i++;
    }
    SqlHelper.print(ROW_ID, sb.toString());
    Cursor c = mDb.rawQuery(sb.toString(), null);
    int id = c.getColumnIndex("rowid");
    c.close();
    close();
    return id;
  }
}