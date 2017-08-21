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
package com.arialyy.aria.core.scheduler;

import android.os.CountDownTimer;
import android.os.Message;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.core.inf.AbsTask;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.queue.ITaskQueue;
import com.arialyy.aria.core.upload.UploadTask;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lyy on 2017/6/4.
 * 事件调度器，用于处理任务状态的调度
 */
abstract class AbsSchedulers<TASK_ENTITY extends AbsTaskEntity, ENTITY extends AbsEntity, TASK extends AbsTask<ENTITY>, QUEUE extends ITaskQueue<TASK, TASK_ENTITY, ENTITY>>
    implements ISchedulers<TASK> {
  private final String TAG = "AbsSchedulers";

  static final int DOWNLOAD = 0xa1;
  static final int UPLOAD = 0xa2;
  static final int DOWNLOAD_GROUP = 0xa3;

  protected QUEUE mQueue;

  private Map<String, ISchedulerListener<TASK>> mSchedulerListeners = new ConcurrentHashMap<>();

  private Map<String, AbsSchedulerListener<TASK>> mObservers = new ConcurrentHashMap<>();

  /**
   * 设置调度器类型
   */
  abstract int getSchedulerType();

  /**
   * 设置代理类后缀名
   */
  abstract String getProxySuffix();

  /**
   * @param targetName 观察者，创建该监听器的对象类名
   * @param schedulerListener {@link ISchedulerListener}
   * @see #register(Object)
   */
  @Deprecated @Override public void addSchedulerListener(String targetName,
      ISchedulerListener<TASK> schedulerListener) {
    mSchedulerListeners.put(targetName, schedulerListener);
  }

  /**
   * @param targetName 观察者，创建该监听器的对象类名
   * @see #unRegister(Object)
   */
  @Override public void removeSchedulerListener(String targetName,
      ISchedulerListener<TASK> schedulerListener) {
    //该内存泄露解决方案：http://stackoverflow.com/questions/14585829/how-safe-is-to-delete-already-removed-concurrenthashmap-element
    for (Iterator<Map.Entry<String, ISchedulerListener<TASK>>> iter =
        mSchedulerListeners.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry<String, ISchedulerListener<TASK>> entry = iter.next();
      if (entry.getKey().equals(targetName)) iter.remove();
    }
  }

  @Override public void register(Object obj) {
    String targetName = obj.getClass().getName();
    AbsSchedulerListener<TASK> listener = mObservers.get(targetName);
    if (listener == null) {
      listener = createListener(targetName);
      if (listener != null) {
        listener.setListener(obj);
        mObservers.put(targetName, listener);
      } else {
        Log.e(TAG, "注册错误，没有【" + targetName + "】观察者");
      }
    }
  }

  @Override public void unRegister(Object obj) {
    for (Iterator<Map.Entry<String, AbsSchedulerListener<TASK>>> iter =
        mObservers.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry<String, AbsSchedulerListener<TASK>> entry = iter.next();
      if (entry.getKey().equals(obj.getClass().getName())) iter.remove();
    }
  }

  /**
   * 创建代理类
   *
   * @param targetName 通过观察者创建对应的Aria事件代理
   */
  private AbsSchedulerListener<TASK> createListener(String targetName) {
    AbsSchedulerListener<TASK> listener = null;
    try {
      Class clazz = Class.forName(targetName + getProxySuffix());
      listener = (AbsSchedulerListener<TASK>) clazz.newInstance();
    } catch (ClassNotFoundException e) {
      Log.e(TAG, targetName + "，没有Aria的Download或Upload注解方法");
    } catch (InstantiationException e) {
      Log.e(TAG, e.getMessage());
    } catch (IllegalAccessException e) {
      Log.e(TAG, e.getMessage());
    }
    return listener;
  }

  @Override public boolean handleMessage(Message msg) {
    TASK task = (TASK) msg.obj;
    if (task == null) {
      Log.e(TAG, "请传入下载任务");
      return true;
    }
    ENTITY entity = task.getEntity();
    switch (msg.what) {
      case STOP:
        if (task.getEntity().getState() == IEntity.STATE_WAIT) {
          break;
        }
      case CANCEL:
        mQueue.removeTask(entity);
        if (mQueue.getCurrentExePoolNum() < AriaManager.getInstance(AriaManager.APP)
            .getUploadConfig()
            .getMaxTaskNum()) {
          startNextTask();
        }
        break;
      case COMPLETE:
        mQueue.removeTask(entity);
        startNextTask();
        break;
      case FAIL:
        handleFailTask(task);
        break;
    }
    callback(msg.what, task);
    return true;
  }

  /**
   * 回调
   *
   * @param state 状态
   */
  private void callback(int state, TASK task) {
    if (mSchedulerListeners.size() > 0) {
      Set<String> keys = mSchedulerListeners.keySet();
      for (String key : keys) {
        callback(state, task, mSchedulerListeners.get(key));
      }
    } else if (mObservers.size() > 0) {
      Set<String> keys = mObservers.keySet();
      for (String key : keys) {
        callback(state, task, mObservers.get(key));
      }
    }
  }

  private void callback(int state, TASK task, ISchedulerListener<TASK> listener) {
    if (listener != null) {
      if (task == null) {
        Log.e(TAG, "TASK 为null，回调失败");
        return;
      }
      switch (state) {
        case PRE:
          listener.onPre(task);
          break;
        case POST_PRE:
          listener.onTaskPre(task);
          break;
        case RUNNING:
          listener.onTaskRunning(task);
          break;
        case START:
          listener.onTaskStart(task);
          break;
        case STOP:
          listener.onTaskStop(task);
          break;
        case RESUME:
          listener.onTaskResume(task);
          break;
        case CANCEL:
          listener.onTaskCancel(task);
          break;
        case COMPLETE:
          listener.onTaskComplete(task);
          break;
        case FAIL:
          listener.onTaskFail(task);
          break;
        case SUPPORT_BREAK_POINT:
          if (listener instanceof IDownloadSchedulerListener) {
            ((IDownloadSchedulerListener<TASK>) listener).onNoSupportBreakPoint(task);
          }
          break;
      }
    }
  }

  /**
   * 处理下载任务下载失败的情形
   *
   * @param task 下载任务
   */
  private void handleFailTask(final TASK task) {
    long interval = 2000;
    int num = 10;
    if (task instanceof DownloadTask) {
      interval = AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getReTryInterval();
      num = AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getReTryNum();
    } else if (task instanceof UploadTask) {
      interval = AriaManager.getInstance(AriaManager.APP).getUploadConfig().getReTryInterval();
      num = AriaManager.getInstance(AriaManager.APP).getUploadConfig().getReTryNum();
    }

    final int reTryNum = num;
    CountDownTimer timer = new CountDownTimer(interval, 1000) {
      @Override public void onTick(long millisUntilFinished) {

      }

      @Override public void onFinish() {
        ENTITY entity = task.getEntity();
        if (entity.getFailNum() < reTryNum) {
          TASK task = mQueue.getTask(entity);
          mQueue.reTryStart(task);
        } else {
          mQueue.removeTask(entity);
          startNextTask();
        }
      }
    };
    timer.start();
  }

  /**
   * 启动下一个任务，条件：任务停止，取消下载，任务完成
   */
  protected void startNextTask() {
    TASK newTask = mQueue.getNextTask();
    if (newTask == null) {
      Log.w(TAG, "没有下一任务");
      return;
    }
    if (newTask.getEntity().getState() == IEntity.STATE_WAIT) {
      mQueue.startTask(newTask);
    }
  }

  /**
   * 是否有下一任务
   *
   * @return {@code true} 有，{@code false} 无
   */
  boolean hasNextTask() {
    return mQueue.getCurrentCachePoolNum() > 0;
  }

  /**
   * 获取正在执行的队列数
   */
  int getExeTaskNum() {
    return mQueue.getCurrentExePoolNum();
  }
}
