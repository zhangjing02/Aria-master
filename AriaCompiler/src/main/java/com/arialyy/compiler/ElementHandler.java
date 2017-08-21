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

import com.arialyy.annotations.Download;
import com.arialyy.annotations.DownloadGroup;
import com.arialyy.annotations.Upload;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Created by lyy on 2017/6/6.
 * 元素处理
 */
class ElementHandler {
  private static final boolean DEBUG = false;

  private Filer mFiler;
  private Elements mElementUtil;
  private Map<String, ProxyMethodParam> mMethods = new HashMap<>();
  private Map<String, Set<String>> mListenerClass = new HashMap<>();

  ElementHandler(Filer filer, Elements elements) {
    mFiler = filer;
    mElementUtil = elements;
  }

  /**
   * VariableElement 一般代表成员变量
   * ExecutableElement 一般代表类中的方法
   * TypeElement 一般代表代表类
   * PackageElement 一般代表Package
   */
  void handleDownload(RoundEnvironment roundEnv) {
    saveMethod(TaskEnum.DOWNLOAD, roundEnv, Download.onNoSupportBreakPoint.class,
        ProxyConstance.TASK_NO_SUPPORT_BREAKPOINT);
    saveMethod(TaskEnum.DOWNLOAD, roundEnv, Download.onPre.class, ProxyConstance.PRE);
    saveMethod(TaskEnum.DOWNLOAD, roundEnv, Download.onTaskCancel.class,
        ProxyConstance.TASK_CANCEL);
    saveMethod(TaskEnum.DOWNLOAD, roundEnv, Download.onTaskComplete.class,
        ProxyConstance.TASK_COMPLETE);
    saveMethod(TaskEnum.DOWNLOAD, roundEnv, Download.onTaskFail.class, ProxyConstance.TASK_FAIL);
    saveMethod(TaskEnum.DOWNLOAD, roundEnv, Download.onTaskPre.class, ProxyConstance.TASK_PRE);
    saveMethod(TaskEnum.DOWNLOAD, roundEnv, Download.onTaskResume.class,
        ProxyConstance.TASK_RESUME);
    saveMethod(TaskEnum.DOWNLOAD, roundEnv, Download.onTaskRunning.class,
        ProxyConstance.TASK_RUNNING);
    saveMethod(TaskEnum.DOWNLOAD, roundEnv, Download.onTaskStart.class, ProxyConstance.TASK_START);
    saveMethod(TaskEnum.DOWNLOAD, roundEnv, Download.onTaskStop.class, ProxyConstance.TASK_STOP);
  }

  /**
   * 处理搜索到的下载任务组注解
   */
  void handleDownloadGroup(RoundEnvironment roundEnv) {
    saveMethod(TaskEnum.DOWNLOAD_GROUP, roundEnv, DownloadGroup.onPre.class, ProxyConstance.PRE);
    saveMethod(TaskEnum.DOWNLOAD_GROUP, roundEnv, DownloadGroup.onTaskCancel.class,
        ProxyConstance.TASK_CANCEL);
    saveMethod(TaskEnum.DOWNLOAD_GROUP, roundEnv, DownloadGroup.onTaskComplete.class,
        ProxyConstance.TASK_COMPLETE);
    saveMethod(TaskEnum.DOWNLOAD_GROUP, roundEnv, DownloadGroup.onTaskFail.class,
        ProxyConstance.TASK_FAIL);
    saveMethod(TaskEnum.DOWNLOAD_GROUP, roundEnv, DownloadGroup.onTaskPre.class,
        ProxyConstance.TASK_PRE);
    saveMethod(TaskEnum.DOWNLOAD_GROUP, roundEnv, DownloadGroup.onTaskResume.class,
        ProxyConstance.TASK_RESUME);
    saveMethod(TaskEnum.DOWNLOAD_GROUP, roundEnv, DownloadGroup.onTaskRunning.class,
        ProxyConstance.TASK_RUNNING);
    saveMethod(TaskEnum.DOWNLOAD_GROUP, roundEnv, DownloadGroup.onTaskStart.class,
        ProxyConstance.TASK_START);
    saveMethod(TaskEnum.DOWNLOAD_GROUP, roundEnv, DownloadGroup.onTaskStop.class,
        ProxyConstance.TASK_STOP);
  }

  /**
   * 处理搜索到的上传注解F
   */
  void handleUpload(RoundEnvironment roundEnv) {
    saveMethod(TaskEnum.UPLOAD, roundEnv, Upload.onNoSupportBreakPoint.class,
        ProxyConstance.TASK_NO_SUPPORT_BREAKPOINT);
    saveMethod(TaskEnum.UPLOAD, roundEnv, Upload.onPre.class, ProxyConstance.PRE);
    saveMethod(TaskEnum.UPLOAD, roundEnv, Upload.onTaskCancel.class, ProxyConstance.TASK_CANCEL);
    saveMethod(TaskEnum.UPLOAD, roundEnv, Upload.onTaskComplete.class,
        ProxyConstance.TASK_COMPLETE);
    saveMethod(TaskEnum.UPLOAD, roundEnv, Upload.onTaskFail.class, ProxyConstance.TASK_FAIL);
    saveMethod(TaskEnum.UPLOAD, roundEnv, Upload.onTaskResume.class, ProxyConstance.TASK_RESUME);
    saveMethod(TaskEnum.UPLOAD, roundEnv, Upload.onTaskRunning.class, ProxyConstance.TASK_RUNNING);
    saveMethod(TaskEnum.UPLOAD, roundEnv, Upload.onTaskStart.class, ProxyConstance.TASK_START);
    saveMethod(TaskEnum.UPLOAD, roundEnv, Upload.onTaskStop.class, ProxyConstance.TASK_STOP);
  }

  /**
   * 在build文件夹中生成如下代码的文件
   * <pre>
   *   <code>
   * package com.arialyy.simple.download;
   *
   * import com.arialyy.aria.core.download.DownloadTask;
   * import com.arialyy.aria.core.scheduler.AbsSchedulerListener;
   *
   * public final class SingleTaskActivity$$DownloadListenerProxy extends
   * AbsSchedulerListener<DownloadTask> {
   * private SingleTaskActivity obj;
   *
   *    public void onPre(final DownloadTask task) {
   *      obj.onPre((DownloadTask)task);
   *    }
   *
   *    public void onTaskStart(final DownloadTask task) {
   *      obj.onStart((DownloadTask)task);
   *    }
   *
   *    public void setListener(final Object obj) {
   *      this.obj = (SingleTaskActivity)obj;
   *    }
   * }
   *   </code>
   * </pre>
   */
  void createProxyFile() {
    try {
      createProxyListenerFile();
      createProxyClassFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 创建事件代理文件
   */
  private void createProxyListenerFile() throws IOException {
    Set<String> keys = mMethods.keySet();
    for (String key : keys) {
      ProxyMethodParam entity = mMethods.get(key);
      for (TaskEnum taskEnum : entity.taskEnums) {
        JavaFile jf =
            JavaFile.builder(entity.packageName, createProxyClass(entity, taskEnum)).build();
        createFile(jf);
      }
    }
  }

  private void createFile(JavaFile jf) throws IOException {
    if (DEBUG) {
      // 如果需要在控制台打印生成的文件，则去掉下面的注释
      jf.writeTo(System.out);
    } else {
      jf.writeTo(mFiler);
    }
  }

  /**
   * 每一种注解对应的类集合
   */
  private void createProxyClassFile() throws IOException {
    Set<String> keys = mListenerClass.keySet();
    TypeSpec.Builder builder = TypeSpec.classBuilder(ProxyConstance.PROXY_COUNTER_NAME)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

    FieldSpec mappingField = FieldSpec.builder(
        ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class),
            ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class))),
        ProxyConstance.PROXY_COUNTER_MAP)
        .addModifiers(Modifier.PRIVATE)
        .initializer("new $T()", HashMap.class)
        .build();
    builder.addField(mappingField);

    //增加构造函数
    CodeBlock.Builder cb = CodeBlock.builder();
    cb.add("Set<String> set = null;\n");
    for (String key : keys) {
      addTypeData(key, mListenerClass.get(key), cb);
    }
    MethodSpec structure =
        MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).addCode(cb.build()).build();
    builder.addMethod(structure);

    builder.addMethod(
        creatMethod(ProxyConstance.COUNT_METHOD_DOWNLOAD, ProxyConstance.COUNT_DOWNLOAD));
    builder.addMethod(creatMethod(ProxyConstance.COUNT_METHOD_UPLOAD, ProxyConstance.COUNT_UPLOAD));
    builder.addMethod(creatMethod(ProxyConstance.COUNT_METHOD_DOWNLOAD_GROUP,
        ProxyConstance.COUNT_DOWNLOAD_GROUP));

    JavaFile jf = JavaFile.builder(ProxyConstance.PROXY_COUNTER_PACKAGE, builder.build()).build();
    createFile(jf);
  }

  /**
   * 创建不同任务类型的代理类集合
   *
   * @param key {@link #addListenerMapping(String, String)}
   */
  private MethodSpec creatMethod(String methodName, String key) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
    ParameterizedTypeName returnName =
        ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class));
    builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .returns(returnName)
        .addCode("return " + ProxyConstance.PROXY_COUNTER_MAP + ".get(\"" + key + "\");\n");
    return builder.build();
  }

  /**
   * 添加每一种注解对应类
   *
   * @param type {@link #addListenerMapping(String, String)}
   */
  private void addTypeData(String type, Set<String> clsNames, CodeBlock.Builder cb) {
    if (clsNames == null || clsNames.isEmpty()) return;
    StringBuilder sb = new StringBuilder();
    sb.append("set = new $T();\n");
    for (String clsName : clsNames) {
      sb.append("set.add(\"").append(clsName).append("\");\n");
    }
    sb.append("typeMapping.put(\"").append(type).append("\", ").append("set);\n");
    cb.add(sb.toString(), ClassName.get(HashSet.class));
  }

  /**
   * 创建代理方法
   *
   * @param taskEnum 任务类型枚举{@link TaskEnum}
   * @param annotation {@link Download}、{@link Upload}
   * @param methodName 被代理类注解的方法名
   */
  private MethodSpec createProxyMethod(TaskEnum taskEnum, Class<? extends Annotation> annotation,
      String methodName) {
    ClassName task = ClassName.get(taskEnum.getPkg(), taskEnum.getClassName());

    ParameterSpec parameterSpec =
        ParameterSpec.builder(task, "task").addModifiers(Modifier.FINAL).build();
    StringBuilder sb = new StringBuilder();
    sb.append("Set<String> keys = keyMapping.get(\"").append(methodName).append("\");\n");
    sb.append("if (keys != null) {\n\tif (keys.contains(task.getKey())) {\n")
        .append("\t\tobj.")
        .append(methodName)
        .append("((")
        .append(taskEnum.getClassName())
        .append(")task);\n")
        .append("\t}\n} else {\n")
        .append("\tobj.")
        .append(methodName)
        .append("((")
        .append(taskEnum.getClassName())
        .append(")task);\n}\n");

    return MethodSpec.methodBuilder(annotation.getSimpleName())
        .addModifiers(Modifier.PUBLIC)
        .returns(void.class)
        .addParameter(parameterSpec)
        .addAnnotation(Override.class)
        .addCode(sb.toString())
        .build();
  }

  /**
   * 创建代理类
   */
  private TypeSpec createProxyClass(ProxyMethodParam entity, TaskEnum taskEnum) {
    TypeSpec.Builder builder = TypeSpec.classBuilder(entity.className + taskEnum.getProxySuffix())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

    //添加被代理的类的字段
    ClassName obj = ClassName.get(entity.packageName, entity.className);
    FieldSpec observerField = FieldSpec.builder(obj, "obj").addModifiers(Modifier.PRIVATE).build();
    builder.addField(observerField);

    //添加url映射表
    FieldSpec mappingField = FieldSpec.builder(
        ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class),
            ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class))),
        "keyMapping").addModifiers(Modifier.PRIVATE).initializer("new $T()", HashMap.class).build();
    builder.addField(mappingField);

    //添加注解方法
    Map<Class<? extends Annotation>, String> temp = entity.methods.get(taskEnum);
    if (temp != null) {
      for (Class<? extends Annotation> annotation : temp.keySet()) {
        MethodSpec method = createProxyMethod(taskEnum, annotation, temp.get(annotation));
        builder.addMethod(method);
      }
    }

    //增加构造函数
    CodeBlock.Builder cb = CodeBlock.builder();
    cb.add("Set<String> set = null;\n");
    for (String methodName : entity.keyMappings.keySet()) {
      Set<String> keys = entity.keyMappings.get(methodName);
      if (keys == null || keys.size() == 0) continue;
      StringBuilder sb = new StringBuilder();
      sb.append("set = new $T();\n");
      for (String key : keys) {
        if (key.isEmpty()) continue;
        sb.append("set.add(\"").append(key).append("\");\n");
      }

      sb.append("keyMapping.put(\"").append(methodName).append("\", ").append("set);\n");
      cb.add(sb.toString(), ClassName.get(HashSet.class));
    }
    MethodSpec structure =
        MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).addCode(cb.build()).build();
    builder.addMethod(structure);

    //添加设置代理的类
    ParameterSpec parameterSpec =
        ParameterSpec.builder(Object.class, "obj").addModifiers(Modifier.FINAL).build();
    MethodSpec listener = MethodSpec.methodBuilder(ProxyConstance.SET_LISTENER)
        .addModifiers(Modifier.PUBLIC)
        .returns(void.class)
        .addParameter(parameterSpec)
        .addAnnotation(Override.class)
        .addCode("this.obj = (" + entity.className + ")obj;\n")
        .build();
    builder.addJavadoc("该文件为Aria自动生成的代理文件，请不要修改该文件的任何代码！\n");

    //创建父类参数
    ClassName superClass = ClassName.get("com.arialyy.aria.core.scheduler", "AbsSchedulerListener");
    //创建泛型
    ClassName typeVariableName = ClassName.get(taskEnum.getPkg(), taskEnum.getClassName());
    builder.superclass(ParameterizedTypeName.get(superClass, typeVariableName));
    builder.addMethod(listener);
    return builder.build();
  }

  void clean() {
    mMethods.clear();
  }

  /**
   * 查找并保存扫描到的方法
   */
  private void saveMethod(TaskEnum taskEnum, RoundEnvironment roundEnv,
      Class<? extends Annotation> annotationClazz, int annotationType) {
    for (Element element : roundEnv.getElementsAnnotatedWith(annotationClazz)) {
      ElementKind kind = element.getKind();
      if (kind == ElementKind.METHOD) {
        ExecutableElement method = (ExecutableElement) element;
        TypeElement classElement = (TypeElement) method.getEnclosingElement();
        PackageElement packageElement = mElementUtil.getPackageOf(classElement);
        checkDownloadMethod(taskEnum, method);
        String methodName = method.getSimpleName().toString();
        String className = method.getEnclosingElement().toString(); //全类名
        ProxyMethodParam proxyEntity = mMethods.get(className);
        if (proxyEntity == null) {
          proxyEntity = new ProxyMethodParam();
          proxyEntity.taskEnums = new HashSet<>();
          proxyEntity.packageName = packageElement.getQualifiedName().toString();
          proxyEntity.className = classElement.getSimpleName().toString();
          mMethods.put(className, proxyEntity);
        }
        proxyEntity.taskEnums.add(taskEnum);
        if (proxyEntity.methods.get(taskEnum) == null) {
          proxyEntity.methods.put(taskEnum, new HashMap<Class<? extends Annotation>, String>());
        }
        proxyEntity.methods.get(taskEnum).put(annotationClazz, methodName);
        proxyEntity.keyMappings.put(methodName, getValues(taskEnum, method, annotationType));
      }
    }
  }

  /**
   * 获取注解的内容
   */
  private Set<String> getValues(TaskEnum taskEnum, ExecutableElement method, int annotationType) {
    String clsName = method.getEnclosingElement().toString();
    String[] keys = null;
    switch (taskEnum) {
      case DOWNLOAD:
        keys = getDownloadValues(method, annotationType);
        addListenerMapping(clsName, ProxyConstance.COUNT_DOWNLOAD);
        break;
      case UPLOAD:
        keys = getUploadValues(method, annotationType);
        addListenerMapping(clsName, ProxyConstance.COUNT_UPLOAD);
        break;
      case DOWNLOAD_GROUP:
        keys = getDownloadGroupValues(method, annotationType);
        addListenerMapping(clsName, ProxyConstance.COUNT_DOWNLOAD_GROUP);
        break;
    }
    return keys == null ? null : convertSet(keys);
  }

  /**
   * 添加方法映射
   *
   * @param clsName 注解事件的类
   * @param key {@link ProxyConstance#COUNT_DOWNLOAD}、{@link ProxyConstance#COUNT_UPLOAD}、{@link
   * ProxyConstance#COUNT_DOWNLOAD_GROUP}
   */
  private void addListenerMapping(String clsName, String key) {
    Set<String> cls = mListenerClass.get(key);
    if (cls == null) {
      cls = new HashSet<>();
      mListenerClass.put(key, cls);
    }
    cls.add(clsName);
  }

  /**
   * 获取下载任务组的注解数据
   */
  private String[] getDownloadGroupValues(ExecutableElement method, int annotationType) {
    String[] values = null;
    switch (annotationType) {
      case ProxyConstance.PRE:
        values = method.getAnnotation(DownloadGroup.onPre.class).value();
        break;
      case ProxyConstance.TASK_PRE:
        values = method.getAnnotation(DownloadGroup.onTaskPre.class).value();
        break;
      case ProxyConstance.TASK_RESUME:
        values = method.getAnnotation(DownloadGroup.onTaskResume.class).value();
        break;
      case ProxyConstance.TASK_START:
        values = method.getAnnotation(DownloadGroup.onTaskStart.class).value();
        break;
      case ProxyConstance.TASK_RUNNING:
        values = method.getAnnotation(DownloadGroup.onTaskRunning.class).value();
        break;
      case ProxyConstance.TASK_STOP:
        values = method.getAnnotation(DownloadGroup.onTaskStop.class).value();
        break;
      case ProxyConstance.TASK_COMPLETE:
        values = method.getAnnotation(DownloadGroup.onTaskComplete.class).value();
        break;
      case ProxyConstance.TASK_CANCEL:
        values = method.getAnnotation(DownloadGroup.onTaskCancel.class).value();
        break;
      case ProxyConstance.TASK_FAIL:
        values = method.getAnnotation(DownloadGroup.onTaskFail.class).value();
        break;
    }
    return values;
  }

  /**
   * 获取上传的注解数据
   */
  private String[] getUploadValues(ExecutableElement method, int annotationType) {
    String[] values = null;
    switch (annotationType) {
      case ProxyConstance.PRE:
        values = method.getAnnotation(Upload.onPre.class).value();
        break;
      case ProxyConstance.TASK_PRE:
        //values = method.getAnnotation(Upload.onTaskPre.class).value();
        break;
      case ProxyConstance.TASK_RESUME:
        values = method.getAnnotation(Upload.onTaskResume.class).value();
        break;
      case ProxyConstance.TASK_START:
        values = method.getAnnotation(Upload.onTaskStart.class).value();
        break;
      case ProxyConstance.TASK_RUNNING:
        values = method.getAnnotation(Upload.onTaskRunning.class).value();
        break;
      case ProxyConstance.TASK_STOP:
        values = method.getAnnotation(Upload.onTaskStop.class).value();
        break;
      case ProxyConstance.TASK_COMPLETE:
        values = method.getAnnotation(Upload.onTaskComplete.class).value();
        break;
      case ProxyConstance.TASK_CANCEL:
        values = method.getAnnotation(Upload.onTaskCancel.class).value();
        break;
      case ProxyConstance.TASK_FAIL:
        values = method.getAnnotation(Upload.onTaskFail.class).value();
        break;
      case ProxyConstance.TASK_NO_SUPPORT_BREAKPOINT:
        //values = method.getAnnotation(Upload.onNoSupportBreakPoint.class).value();
        break;
    }
    return values;
  }

  /**
   * 获取下载的注解数据
   */
  private String[] getDownloadValues(ExecutableElement method, int annotationType) {
    String[] values = null;
    switch (annotationType) {
      case ProxyConstance.PRE:
        values = method.getAnnotation(Download.onPre.class).value();
        break;
      case ProxyConstance.TASK_PRE:
        values = method.getAnnotation(Download.onTaskPre.class).value();
        break;
      case ProxyConstance.TASK_RESUME:
        values = method.getAnnotation(Download.onTaskResume.class).value();
        break;
      case ProxyConstance.TASK_START:
        values = method.getAnnotation(Download.onTaskStart.class).value();
        break;
      case ProxyConstance.TASK_RUNNING:
        values = method.getAnnotation(Download.onTaskRunning.class).value();
        break;
      case ProxyConstance.TASK_STOP:
        values = method.getAnnotation(Download.onTaskStop.class).value();
        break;
      case ProxyConstance.TASK_COMPLETE:
        values = method.getAnnotation(Download.onTaskComplete.class).value();
        break;
      case ProxyConstance.TASK_CANCEL:
        values = method.getAnnotation(Download.onTaskCancel.class).value();
        break;
      case ProxyConstance.TASK_FAIL:
        values = method.getAnnotation(Download.onTaskFail.class).value();
        break;
      case ProxyConstance.TASK_NO_SUPPORT_BREAKPOINT:
        values = method.getAnnotation(Download.onNoSupportBreakPoint.class).value();
        break;
    }
    return values;
  }

  /**
   * 检查和下载相关的方法，如果被注解的方法为private或参数不合法，则抛异常
   */
  private void checkDownloadMethod(TaskEnum taskEnum, ExecutableElement method) {
    String methodName = method.getSimpleName().toString();
    String className = method.getEnclosingElement().toString();
    Set<Modifier> modifiers = method.getModifiers();
    if (modifiers.contains(Modifier.PRIVATE)) {
      throw new IllegalAccessError(className + "." + methodName + "不能为private方法");
    }
    List<VariableElement> params = (List<VariableElement>) method.getParameters();
    if (params.size() > 1) {
      throw new IllegalArgumentException(
          className + "." + methodName + "参数错误, 参数只有一个，且参数必须是" + getCheckParams(taskEnum));
    }
    if (!params.get(0).asType().toString().equals(getCheckParams(taskEnum))) {
      throw new IllegalArgumentException(className
          + "."
          + methodName
          + "参数【"
          + params.get(0).getSimpleName()
          + "】类型错误，参数必须是"
          + getCheckParams(taskEnum));
    }
  }

  /**
   * 字符串数组转set
   *
   * @param keys 注解中查到的key
   */
  private Set<String> convertSet(final String[] keys) {
    if (keys == null || keys.length == 0) {
      return null;
    }
    if (keys[0].isEmpty()) return null;
    Set<String> set = new HashSet<>();
    Collections.addAll(set, keys);
    return set;
  }

  private String getCheckParams(TaskEnum taskEnum) {
    return taskEnum.pkg + "." + taskEnum.getClassName();
  }
}
