# Aria
![图标](https://github.com/AriaLyy/DownloadUtil/blob/master/app/src/main/res/mipmap-hdpi/ic_launcher.png)</br>

## [中文文档](https://github.com/AriaLyy/Aria/blob/master/CHINESE_README.md)

Aria project is from the moment taht the work encountered in a file download management needs adn i was tortured at the time of the pain.</br>
Since then i have a idea which is to program a simple and easy to use,stable and efficient download framework. 
Aria experienced 1.0 to 3.0 development, be more and more close to the original set by the target.

Aria has the following characteristics：
 + simple and convenient
   - can be used in Activity, Service, Fragment, Dialog, popupWindow, Notification and other components
   - support the task of automatic scheduling, the user does not need to care about the state of the task switch logic
   - [Through the Aria event, it is easy to get the download status of the current download task](#status)
   - [a code plus can get the current download speed](#interface)
   - [a code can be dynamically set the maximum number of downloads](#parameters)
   - [code to achieve speed limit](#interface)
   - [It is easy to modify the number of download threads by modifying the configuration file](https://github.com/AriaLyy/Aria/blob/master/app/src/main/assets/aria_config.xml)
   - [priority to download a task](#interface)
   
 + Support https address download
   - It is easy to set the CA certificate information in the configuration file
 + Support 300,301,302 redirect download link download
 + Support upload operation

How do we to use Aria?
* [download](#Using)
* [upload](#Upload)

If you feel that Aria is helpful to you, your star and issues will be the greatest support for me.`^_^`

## Download
[![Download](https://api.bintray.com/packages/arialyy/maven/AriaApi/images/download.svg)](https://bintray.com/arialyy/maven/AriaApi/_latestVersion)
[![Download](https://api.bintray.com/packages/arialyy/maven/AriaCompiler/images/download.svg)](https://bintray.com/arialyy/maven/AriaCompiler/_latestVersion)
```java
compile 'com.arialyy.aria:aria-core:3.2.0'
annotationProcessor 'com.arialyy.aria:aria-compiler:3.2.0'
```

## For example
![Multi-task download](https://github.com/AriaLyy/DownloadUtil/blob/master/img/download_img.gif)
![download speed limit](https://github.com/AriaLyy/DownloadUtil/blob/master/img/max_speed.gif)

## performance
![Performance display](https://github.com/AriaLyy/DownloadUtil/blob/master/img/performance.png)

***
## Using
Since Aria involves the operation of files and networks, you need to add the following permissions to the manifest file.
```xml
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

## Use Aria to download
* Add a task (do not download), when other download tasks are completed, will automatically download the waiting task
  ```java
  Aria.download(this)
      .load(DOWNLOAD_URL)
      .setDownloadPath(DOWNLOAD_PATH)	//file save path
      .add();
  ```

* download

  ```java
  Aria.download(this)
      .load(DOWNLOAD_URL)     //load download url
      .setDownloadPath(DOWNLOAD_PATH)    //file save path
      .start();   //start download
  ```
* Pause

  ```java
  Aria.download(this).load(DOWNLOAD_URL).pause();
  ```
* Resume download

  ```java
  Aria.download(this).load(DOWNLOAD_URL).resume();
  ```
* Cancel download

  ```java
  Aria.download(this).load(DOWNLOAD_URL).cancel();
  ```

### status
If you want to read the download progress or download the information, then you need to create an event class and register the event class into the Aria manager in the onResume (Activity, Fragment) or constructor (Dialog, PopupWindow).

1. Register the object to Aria

 `Aria.download(this).register();` or `Aria.upload(this).register();`
 ```java
 @Override
 protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Aria.download(this).register();
  }
 ```

2. Use`@Download` or `@Upload` to annotate your function<br>
  **note：**
   - Annotation is done by using `Apt`, so you do not need to worry that this will affect your machine's performance
   - The annotated method**can not be modified by private**
   - The annotated method**can have only one argument, and the parameter type must be either`DownloadTask` or `UploadTask`**
   - Method name can be any string

3. In addition to using annotation methods in widget (Activity, Fragment, Dialog, Popupwindow), you can also use annotation functions in components such as Service, Notification, and so on.

  ```java
  @Download.onPre(DOWNLOAD_URL)
  protected void onPre(DownloadTask task) {}

  @Download.onTaskStart
  void taskStart(DownloadTask task) {}

  @Download.onTaskRunning
  protected void running(DownloadTask task) {}

  @Download.onTaskResume
  void taskResume(DownloadTask task) {}

  @Download.onTaskStop
  void taskStop(DownloadTask task) {}

  @Download.onTaskCancel
  void taskCancel(DownloadTask task) {}

  @Download.onTaskFail
  void taskFail(DownloadTask task) {}

  @Download.onTaskComplete
  void taskComplete(DownloadTask task) {}

  @Download.onNoSupportBreakPoint
  public void onNoSupportBreakPoint(DownloadTask task) {}

  ```
4. If you want to set up a listener for a single task, or for some specific task.<br>
 **Adding a download address for a task in an annotation means that only the task triggers the annotated method.**

 ```java
 @Download.onTaskRunning({
      "https://test.xx.apk",
      "http://test.xx2.apk"
  }) void taskRunning(DownloadTask task) {
    mAdapter.setProgress(task.getDownloadEntity());
  }
 ```
In the above example，only the download address is`https://test.xx.apk` and `http://test.xx2.apk`will trigger the`taskRunning(DownloadTask task)`method。

### parameters
#### [Configuration file setting parameters](https://github.com/AriaLyy/Aria/blob/master/app/src/main/assets/aria_config.xml)
#### Set the parameters in the code
In addition to the file mode to modify the Aria parameter, the same, you can also modify the code in the Aria parameters</br>
Get the configuration file directly through`Aria.get(this).getDownloadConfig()`or`Aria.get(this).getUploadConfig()`</br>
and then modify the parameters：
```java
// 修改最大下载数，调用完成后，立即生效
// 如当前下载任务数是4，修改完成后，当前任务数会被Aria自动调度任务数
Aria.get(this).getDownloadConfig().setMaxTaskNum(3);
```

### interface
* Stop all tasks

 ```java
 Aria.download(this).stopAllTask();
 ```

* Restore all stopped tasks

 ```java
Aria.download(this).resumeAllTask();
 ```

* Delete all tasks

 ```java
 Aria.download(this).removeAllTask();
 ```

* Maximum download speed limit
 ```java
 //单位为 kb
 Aria.download(this).setMaxSpeed(speed);
 ```

* Get download speed for current tasks<br>
Speed parameters a bit special，need to [download the event support](#status)
``` java
@Override public void onTaskRunning(DownloadTask task) {
  //If you turn on the speed unit conversion configuration, you can get the download speed with units in the following ways, such as: 1 mb/s
  String convertSpeed = task.getConvertSpeed();
  //If you have your own unit format, you can get the original byte length by the following method
  long speed = task.getSpeed();
}
```

* Get the downloaded file size, the current progress percentage</br>
Likewise, you can also get the downloaded file size in the DownloadTask object
```
@Override public void onTaskRunning(DownloadTask task) {
  //Get the file size
  long fileSize = task.getFileSize();
  //Get the file size after conversion
  String fileSize1 = task.getConvertFileSize();
  //The current percentage of progress
  int percent = task.getPercent();
}
```

* Set the high priority task<br>
 If you want to give priority to download a task, you can
``` java
Aria.download(this).load(DOWNLOAD_URL).setDownloadPath(PATH).setHighestPriority();
```

* Set the extension field<br>
 Sometimes, you may want to store some of your own data when you download it</br>
**TIP**: If you have more data, or the data is more complex, you can first convert the data to JSON, and then save it to Aria's download entity
```java
Aria.download(this).load(DOWNLOAD_URL).setExtendField(str)
```

## Upload

 * Add a task (add only, do not upload)

 ```java
 Aria.upload(this)
     .load(filePath)     //file path
     .setUploadUrl(uploadUrl)  // upload the path
     .setAttachment(fileKey)   //The server reads the file's key
     .add();
 ```

 * Upload

 ```java
 Aria.upload(this)
     .load(filePath)     //file path
     .setUploadUrl(uploadUrl)  //upload the path
     .setAttachment(fileKey)   //The server reads the file's key
     .start();
 ```
 * cancel upload

 ```java
 Aria.upload(this).load(filePath).cancel();
 ```

## Confused configuration
```
-dontwarn com.arialyy.aria.**
-keep class com.arialyy.aria.**{*;}
-keep class **$$DownloadListenerProxy{ *; }
-keep class **$$UploadListenerProxy{ *; }
-keepclasseswithmembernames class * {
    @Download.* <methods>;
    @Upload.* <methods>;
}

```

## others
 Have any questions that can give me a message in the[issues](https://github.com/AriaLyy/Aria/issues)。

***

## 后续版本开发规划
* ~~http、scoket断点上传~~
* ~~实现上传队列调度功能~~


## Development log
  + v_3.1.9 Repair the stopAll queue without task when the problem of collapse, increase the function for a single task monitor
  + v_3.1.7 repair some files can not download the bug, increase the apt annotation method, the incident is more simple
  + v_3.1.6 When the task is canceled ontaskCancel callback twice
  + v_3.1.5 Optimize the code structure, increase the priority download task function.
  + v_3.1.4 Repair the fast switching, pause, and restore functions, the probability of re-download problems, add onPre () callback, onPre () used to request the interface before the implementation of interface UI update operation.
  + v_3.1.0 Add the Aria configuration file to optimize the code
  + v_3.0.3 Repair the pause after deleting the task, flashing the problem, add the api to delete the record
  + v_3.0.2 supports 30x redirect link download
  + v_3.0.0 add upload task support to fix some bugs that have been discovered

License
-------

    Copyright 2016 AriaLyy(https://github.com/AriaLyy/Aria)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
