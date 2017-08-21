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

package com.arialyy.aria.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.command.normal.NormalCmdFactory;
import com.arialyy.aria.core.command.normal.AbsNormalCmd;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Created by lyy on 2016/1/22.
 */
public class CommonUtil {
  private static final String TAG = "CommonUtil";

  /**
   * 转换Url
   *
   * @param url 原地址
   * @return 转换后的地址
   */
  public static String convertUrl(String url) {
    if (hasDoubleCharacter(url)) {
      //匹配双字节字符(包括汉字在内)
      String regex = "[^\\x00-\\xff]";
      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(url);
      Set<String> strs = new HashSet<>();
      while (m.find()) {
        strs.add(m.group());
      }
      try {
        for (String str : strs) {
          url = url.replaceAll(str, URLEncoder.encode(str, "UTF-8"));
        }
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
    return url;
  }

  /**
   * 判断是否有双字节字符(包括汉字在内)
   *
   * @param chineseStr 需要进行判断的字符串
   * @return {@code true}有双字节字符，{@code false} 无双字节字符
   */
  public static boolean hasDoubleCharacter(String chineseStr) {
    char[] charArray = chineseStr.toCharArray();
    for (char aCharArray : charArray) {
      if ((aCharArray >= 0x0391) && (aCharArray <= 0xFFE5)) {
        return true;
      }
    }
    return false;
  }

  /**
   * base64 解密字符串
   *
   * @param str 被加密的字符串
   * @return 解密后的字符串
   */
  public static String decryptBASE64(String str) {
    return new String(Base64.decode(str.getBytes(), Base64.DEFAULT));
  }

  /**
   * base64 加密字符串
   *
   * @param str 需要加密的字符串
   * @return 加密后的字符串
   */
  public static String encryptBASE64(String str) {
    return Base64.encodeToString(str.getBytes(), Base64.DEFAULT);
  }

  /**
   * 字符串编码转换
   */
  public static String strCharSetConvert(String oldStr, String charSet) {
    try {
      return new String(oldStr.getBytes(), charSet);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 实例化泛型的实际类型参数
   *
   * @throws Exception
   */
  public static void typeCheck(Type type) throws Exception {
    System.out.println("该类型是" + type);
    // 参数化类型
    if (type instanceof ParameterizedType) {
      Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
      for (int i = 0; i < typeArguments.length; i++) {
        // 类型变量
        if (typeArguments[i] instanceof TypeVariable) {
          System.out.println("第" + (i + 1) + "个泛型参数类型是类型变量" + typeArguments[i] + "，无法实例化。");
        }
        // 通配符表达式
        else if (typeArguments[i] instanceof WildcardType) {
          System.out.println("第" + (i + 1) + "个泛型参数类型是通配符表达式" + typeArguments[i] + "，无法实例化。");
        }
        // 泛型的实际类型，即实际存在的类型
        else if (typeArguments[i] instanceof Class) {
          System.out.println("第" + (i + 1) + "个泛型参数类型是:" + typeArguments[i] + "，可以直接实例化对象");
        }
      }
      // 参数化类型数组或类型变量数组
    } else if (type instanceof GenericArrayType) {
      System.out.println("该泛型类型是参数化类型数组或类型变量数组，可以获取其原始类型。");
      Type componentType = ((GenericArrayType) type).getGenericComponentType();
      // 类型变量
      if (componentType instanceof TypeVariable) {
        System.out.println("该类型变量数组的原始类型是类型变量" + componentType + "，无法实例化。");
      }
      // 参数化类型，参数化类型数组或类型变量数组
      // 参数化类型数组或类型变量数组也可以是多维的数组，getGenericComponentType()方法仅仅是去掉最右边的[]
      else {
        // 递归调用方法自身
        typeCheck(componentType);
      }
    } else if (type instanceof TypeVariable) {
      System.out.println("该类型是类型变量");
    } else if (type instanceof WildcardType) {
      System.out.println("该类型是通配符表达式");
    } else if (type instanceof Class) {
      System.out.println("该类型不是泛型类型");
    } else {
      throw new Exception();
    }
  }

  /**
   * 根据下载任务组的url创建key
   *
   * @return urls 为 null 或者 size为0，返回""
   */
  public static String getMd5Code(List<String> urls) {
    if (urls == null || urls.size() < 1) return "";
    String md5 = "";
    StringBuilder sb = new StringBuilder();
    for (String url : urls) {
      sb.append(url);
    }
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(sb.toString().getBytes());
      md5 = new BigInteger(1, md.digest()).toString(16);
    } catch (NoSuchAlgorithmException e) {
      Log.e(TAG, e.getMessage());
    }
    return md5;
  }

  /**
   * 删除上传任务的配置，包括
   *
   * @param removeFile {@code true} 不仅删除任务数据库记录，还会删除已经删除完成的文件
   * {@code false}如果任务已经完成，只删除任务数据库记录
   */
  public static void delUploadTaskConfig(boolean removeFile, UploadTaskEntity tEntity) {
    UploadEntity uEntity = tEntity.getEntity();
    File file = new File(uEntity.getFilePath());
    if (removeFile) {
      if (file.exists()) {
        file.delete();
      }
    }
    File config = new File(getFileConfig(false, uEntity.getFileName()));
    if (config.exists()) {
      config.delete();
    }
    uEntity.deleteData();
    tEntity.deleteData();
  }

  /**
   * 删除下载任务的配置，包括
   *
   * @param removeFile {@code true} 不仅删除任务数据库记录，还会删除已经下载完成的文件
   * {@code false}如果任务已经完成，只删除任务数据库记录
   */
  public static void delDownloadTaskConfig(boolean removeFile, DownloadTaskEntity tEntity) {
    DownloadEntity dEntity = tEntity.getEntity();
    File file = new File(dEntity.getDownloadPath());
    if (removeFile) {
      if (file.exists()) {
        file.delete();
      }
    } else {
      if (!dEntity.isComplete()) {
        if (file.exists()) {
          file.delete();
        }
      }
    }

    File config = new File(getFileConfig(true, dEntity.getFileName()));
    if (config.exists()) {
      config.delete();
    }
    dEntity.deleteData();
    tEntity.deleteData();
  }

  /**
   * 获取CPU核心数
   */
  public static int getCoresNum() {
    //Private Class to display only CPU devices in the directory listing
    class CpuFilter implements FileFilter {
      @Override public boolean accept(File pathname) {
        //Check if filename is "cpu", followed by a single digit number
        return Pattern.matches("cpu[0-9]", pathname.getName());
      }
    }

    try {
      //Get directory containing CPU info
      File dir = new File("/sys/devices/system/cpu/");
      //Filter to only list the devices we care about
      File[] files = dir.listFiles(new CpuFilter());
      Log.d(TAG, "CPU Count: " + files.length);
      //Return the number of cores (virtual CPU devices)
      return files.length;
    } catch (Exception e) {
      //Print exception
      Log.d(TAG, "CPU Count: Failed.");
      e.printStackTrace();
      //Default to return 1 core
      return 1;
    }
  }

  /**
   * 通过流创建文件
   */
  public static void createFileFormInputStream(InputStream is, String path) {
    try {
      FileOutputStream fos = new FileOutputStream(path);
      byte[] buf = new byte[1376];
      while (is.read(buf) > 0) {
        fos.write(buf, 0, buf.length);
      }
      is.close();
      fos.flush();
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 校验文件MD5码
   */
  public static boolean checkMD5(String md5, File updateFile) {
    if (TextUtils.isEmpty(md5) || updateFile == null) {
      Log.e(TAG, "MD5 string empty or updateFile null");
      return false;
    }

    String calculatedDigest = getFileMD5(updateFile);
    if (calculatedDigest == null) {
      Log.e(TAG, "calculatedDigest null");
      return false;
    }
    return calculatedDigest.equalsIgnoreCase(md5);
  }

  /**
   * 校验文件MD5码
   */
  public static boolean checkMD5(String md5, InputStream is) {
    if (TextUtils.isEmpty(md5) || is == null) {
      Log.e(TAG, "MD5 string empty or updateFile null");
      return false;
    }

    String calculatedDigest = getFileMD5(is);
    if (calculatedDigest == null) {
      Log.e(TAG, "calculatedDigest null");
      return false;
    }
    return calculatedDigest.equalsIgnoreCase(md5);
  }

  /**
   * 获取文件MD5码
   */
  public static String getFileMD5(File updateFile) {
    InputStream is;
    try {
      is = new FileInputStream(updateFile);
    } catch (FileNotFoundException e) {
      Log.e(TAG, "Exception while getting FileInputStream", e);
      return null;
    }

    return getFileMD5(is);
  }

  /**
   * 获取文件MD5码
   */
  public static String getFileMD5(InputStream is) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      Log.e(TAG, "Exception while getting digest", e);
      return null;
    }

    byte[] buffer = new byte[8192];
    int read;
    try {
      while ((read = is.read(buffer)) > 0) {
        digest.update(buffer, 0, read);
      }
      byte[] md5sum = digest.digest();
      BigInteger bigInt = new BigInteger(1, md5sum);
      String output = bigInt.toString(16);
      // Fill to 32 chars
      output = String.format("%32s", output).replace(' ', '0');
      return output;
    } catch (IOException e) {
      throw new RuntimeException("Unable to process file for MD5", e);
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        Log.e(TAG, "Exception on closing MD5 input stream", e);
      }
    }
  }

  public static <T extends AbsTaskEntity> AbsNormalCmd createCmd(String target, T entity, int cmd) {
    return NormalCmdFactory.getInstance().createCmd(target, entity, cmd);
  }

  /**
   * 创建隐性的Intent
   */
  public static Intent createIntent(String packageName, String action) {
    Uri.Builder builder = new Uri.Builder();
    builder.scheme(packageName);
    Uri uri = builder.build();
    Intent intent = new Intent(action);
    intent.setData(uri);
    return intent;
  }

  /**
   * 存储字符串到配置文件
   *
   * @param preName 配置文件名
   * @param key 存储的键值
   * @param value 需要存储的字符串
   * @return 成功标志
   */
  public static Boolean putString(String preName, Context context, String key, String value) {
    SharedPreferences pre = context.getSharedPreferences(preName, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = pre.edit();
    editor.putString(key, value);
    return editor.commit();
  }

  /**
   * 从配置文件读取字符串
   *
   * @param preName 配置文件名
   * @param key 字符串键值
   * @return 键值对应的字符串, 默认返回""
   */
  public static String getString(String preName, Context context, String key) {
    SharedPreferences pre = context.getSharedPreferences(preName, Context.MODE_PRIVATE);
    return pre.getString(key, "");
  }

  /**
   * 获取所有字段，包括父类的字段
   */
  public static List<Field> getAllFields(Class clazz) {
    List<Field> fields = new ArrayList<>();
    Class personClazz = clazz.getSuperclass();
    if (personClazz != null) {
      Class rootClazz = personClazz.getSuperclass();
      if (rootClazz != null) {
        Collections.addAll(fields, rootClazz.getDeclaredFields());
      }
      Collections.addAll(fields, personClazz.getDeclaredFields());
    }
    Collections.addAll(fields, clazz.getDeclaredFields());
    return fields;
  }

  /**
   * 获取当前类里面的所在字段
   */
  public static Field[] getFields(Class clazz) {
    Field[] fields = null;
    fields = clazz.getDeclaredFields();
    if (fields == null || fields.length == 0) {
      Class superClazz = clazz.getSuperclass();
      if (superClazz != null) {
        fields = getFields(superClazz);
      }
    }
    return fields;
  }

  /**
   * 获取类里面的指定对象，如果该类没有则从父类查询
   */
  public static Field getField(Class clazz, String name) {
    Field field = null;
    try {
      field = clazz.getDeclaredField(name);
    } catch (NoSuchFieldException e) {
      try {
        field = clazz.getField(name);
      } catch (NoSuchFieldException e1) {
        if (clazz.getSuperclass() == null) {
          return field;
        } else {
          field = getField(clazz.getSuperclass(), name);
        }
      }
    }
    if (field != null) {
      field.setAccessible(true);
    }
    return field;
  }

  /**
   * 字符串转hashcode
   */
  public static int keyToHashCode(String str) {
    int total = 0;
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      if (ch == '-') ch = (char) 28; // does not contain the same last 5 bits as any letter
      if (ch == '\'') ch = (char) 29; // nor this
      total = (total * 33) + (ch & 0x1F);
    }
    return total;
  }

  /**
   * 将key转换为16进制码
   *
   * @param key 缓存的key
   * @return 转换后的key的值, 系统便是通过该key来读写缓存
   */
  public static String keyToHashKey(String key) {
    String cacheKey;
    try {
      final MessageDigest mDigest = MessageDigest.getInstance("MD5");
      mDigest.update(key.getBytes());
      cacheKey = bytesToHexString(mDigest.digest());
    } catch (NoSuchAlgorithmException e) {
      cacheKey = String.valueOf(key.hashCode());
    }
    return cacheKey;
  }

  /**
   * 将普通字符串转换为16位进制字符串
   */
  public static String bytesToHexString(byte[] src) {
    StringBuilder stringBuilder = new StringBuilder("0x");
    if (src == null || src.length <= 0) {
      return null;
    }
    char[] buffer = new char[2];
    for (byte aSrc : src) {
      buffer[0] = Character.forDigit((aSrc >>> 4) & 0x0F, 16);
      buffer[1] = Character.forDigit(aSrc & 0x0F, 16);
      stringBuilder.append(buffer);
    }
    return stringBuilder.toString();
  }

  /**
   * 获取对象名
   *
   * @param obj 对象
   * @return 对象名
   */
  public static String getClassName(Object obj) {
    String arrays[] = obj.getClass().getName().split("\\.");
    return arrays[arrays.length - 1];
  }

  /**
   * 获取对象名
   *
   * @param clazz clazz
   * @return 对象名
   */
  public static String getClassName(Class clazz) {
    String arrays[] = clazz.getName().split("\\.");
    return arrays[arrays.length - 1];
  }

  /**
   * 格式化文件大小
   *
   * @param size file.length() 获取文件大小
   */
  public static String formatFileSize(double size) {
    if (size < 0) {
      return "0kb";
    }
    double kiloByte = size / 1024;
    if (kiloByte < 1) {
      return size + "b";
    }

    double megaByte = kiloByte / 1024;
    if (megaByte < 1) {
      BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
      return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "kb";
    }

    double gigaByte = megaByte / 1024;
    if (gigaByte < 1) {
      BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
      return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "mb";
    }

    double teraBytes = gigaByte / 1024;
    if (teraBytes < 1) {
      BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
      return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "gb";
    }
    BigDecimal result4 = new BigDecimal(teraBytes);
    return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "tb";
  }

  /**
   * 创建目录 当目录不存在的时候创建文件，否则返回false
   */
  public static boolean createDir(String path) {
    File file = new File(path);
    if (!file.exists()) {
      if (!file.mkdirs()) {
        Log.d(TAG, "创建失败，请检查路径和是否配置文件权限！");
        return false;
      }
      return true;
    }
    return false;
  }

  /**
   * 创建文件
   * 当文件不存在的时候就创建一个文件。
   * 如果文件存在，先删除原文件，然后重新创建一个新文件
   */
  public static void createFile(String path) {
    if (TextUtils.isEmpty(path)) {
      Log.e(TAG, "文件路径不能为null");
      return;
    }
    File file = new File(path);
    if (!file.getParentFile().exists()) {
      Log.d(TAG, "目标文件所在路径不存在，准备创建……");
      if (!createDir(file.getParent())) {
        Log.d(TAG, "创建目录文件所在的目录失败！文件路径【" + path + "】");
      }
    }
    // 创建目标文件
    if (file.exists()) {
      final File to = new File(file.getAbsolutePath() + System.currentTimeMillis());
      file.renameTo(to);
      to.delete();
    }
    try {
      if (file.createNewFile()) {
        Log.d(TAG, "创建文件成功:" + file.getAbsolutePath());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 设置打印的异常格式
   */
  public static String getPrintException(Throwable ex) {
    StringBuilder err = new StringBuilder();
    err.append("ExceptionDetailed:\n");
    err.append("====================Exception Info====================\n");
    err.append(ex.toString());
    err.append("\n");
    StackTraceElement[] stack = ex.getStackTrace();
    for (StackTraceElement stackTraceElement : stack) {
      err.append(stackTraceElement.toString()).append("\n");
    }
    Throwable cause = ex.getCause();
    if (cause != null) {
      err.append("【Caused by】: ");
      err.append(cause.toString());
      err.append("\n");
      StackTraceElement[] stackTrace = cause.getStackTrace();
      for (StackTraceElement stackTraceElement : stackTrace) {
        err.append(stackTraceElement.toString()).append("\n");
      }
    }
    err.append("===================================================");
    return err.toString();
  }

  /**
   * 通过文件名获取下载配置文件
   *
   * @param fileName 文件名
   */
  public static String getFileConfig(boolean isDownload, String fileName) {
    return AriaManager.APP.getFilesDir().getPath() + (isDownload ? AriaManager.DOWNLOAD_TEMP_DIR
        : AriaManager.UPLOAD_TEMP_DIR) + fileName + ".properties";
  }

  /**
   * 重命名下载配置文件
   * 如果旧的配置文件名不存在，则使用新的配置文件名新创建一个文件，否则将旧的配置文件重命名为新的位置文件名。
   * 除了重命名配置文件名外，还会将文件中的记录重命名为新的记录，如果没有记录，则不做处理
   *
   * @param oldName 旧的下载文件名
   * @param newName 新的下载文件名
   */
  public static void renameDownloadConfig(String oldName, String newName) {
    renameConfig(true, oldName, newName);
  }

  /**
   * 重命名上传配置文件
   * 如果旧的配置文件名不存在，则使用新的配置文件名新创建一个文件，否则将旧的配置文件重命名为新的位置文件名。
   * 除了重命名配置文件名外，还会将文件中的记录重命名为新的记录，如果没有记录，则不做处理
   *
   * @param oldName 旧的上传文件名
   * @param newName 新的上传文件名
   */
  public static void renameUploadConfig(String oldName, String newName) {
    renameConfig(false, oldName, newName);
  }

  private static void renameConfig(boolean isDownload, String oldName, String newName) {
    if (oldName.equals(newName)) return;
    File oldFile = new File(getFileConfig(isDownload, oldName));
    File newFile = new File(getFileConfig(isDownload, oldName));
    if (!oldFile.exists()) {
      createFile(newFile.getPath());
    } else {
      Properties pro = CommonUtil.loadConfig(oldFile);
      if (!pro.isEmpty()) {
        Set<Object> keys = pro.keySet();
        Set<String> newKeys = new LinkedHashSet<>();
        Set<String> values = new LinkedHashSet<>();
        for (Object key : keys) {
          String oldKey = String.valueOf(key);
          if (oldKey.contains(oldName)) {
            values.add(pro.getProperty(oldKey));
            newKeys.add(oldKey.replace(oldName, newName));
          }
        }

        pro.clear();
        Iterator<String> next = values.iterator();
        for (String key : newKeys) {
          pro.setProperty(key, next.next());
        }

        CommonUtil.saveConfig(oldFile, pro);
      }

      oldFile.renameTo(newFile);
    }
  }

  /**
   * 读取下载配置文件
   */
  public static Properties loadConfig(File file) {
    Properties properties = new Properties();
    FileInputStream fis = null;
    if (!file.exists()) {
      createFile(file.getPath());
    }
    try {
      fis = new FileInputStream(file);
      properties.load(fis);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (fis != null) {
          fis.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return properties;
  }

  /**
   * 保存配置文件
   */
  public static void saveConfig(File file, Properties properties) {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file, false);
      properties.store(fos, null);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (fos != null) {
          fos.flush();
          fos.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}