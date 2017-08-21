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
package com.arialyy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by lyy on 2017/6/6.
 * Aria下载事件被注解的方法中，参数仅能有一个，参数类型为{@link com.arialyy.aria.core.upload.UploadTask}
 * <pre>
 *   <code>
 *      {@literal @}Upload.onPre
 *       protected void onPre(UploadTask task) {
 *        L.d(TAG, "fileSize = " + task.getConvertFileSize());
 *       }
 *   </code>
 * </pre>
 * {@literal @}Upload.onPre("/mnt/sdcard/test.zip")，如果你的注解中增加了path描述，
 * 则表示，所有上传任务中，只有上传路径为"/mnt/sdcard/test.zip"的任务才能回调该注解的方法。
 */
@Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface Upload {

  /**
   * 如果你在方法中添加{@code @Upload.onPre}注解，在预处理完成时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) @interface onPre {
    String[] value() default { AriaConstance.NO_URL };
  }

  /**
   * 如果你在方法中添加{@code @Upload.onTaskResume}注解，在任务恢复下载时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) @interface onTaskResume {
    String[] value() default { AriaConstance.NO_URL };
  }

  /**
   * 如果你在方法中添加{@code @Upload.onTaskStart}注解，在任务开始下载时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) @interface onTaskStart {
    String[] value() default { AriaConstance.NO_URL };
  }

  /**
   * 如果你在方法中添加{@code @Upload.onTaskStop}注解，在任务停止时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) @interface onTaskStop {
    String[] value() default { AriaConstance.NO_URL };
  }

  /**
   * 如果你在方法中添加{@code @Upload.onTaskCancel}l注解，在任务取消时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) @interface onTaskCancel {
    String[] value() default { AriaConstance.NO_URL };
  }

  /**
   * 如果你在方法中添加{@code @Upload.onTaskFail)注解，在任务预失败时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) @interface onTaskFail {
    String[] value() default { AriaConstance.NO_URL };
  }

  /**
   * 如果你在方法中添加{@code @Upload.onTaskComplete}注解，在任务完成时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) @interface onTaskComplete {
    String[] value() default { AriaConstance.NO_URL };
  }

  /**
   * 如果你在方法中添加{@code @Upload.onTaskRunning}注解，在任务正在下载，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) @interface onTaskRunning {
    String[] value() default { AriaConstance.NO_URL };
  }

  /**
   * 如果你在方法中添加{@code @Upload.onNoSupportBreakPoint}注解，如果该任务不支持断点，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) @interface onNoSupportBreakPoint {
    String[] value() default { AriaConstance.NO_URL };
  }
}
