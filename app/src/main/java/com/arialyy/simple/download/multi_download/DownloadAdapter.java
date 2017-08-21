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

package com.arialyy.simple.download.multi_download;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.simple.R;
import com.arialyy.simple.base.adapter.AbsHolder;
import com.arialyy.simple.base.adapter.AbsRVAdapter;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;
import com.arialyy.simple.widget.SubStateLinearLayout;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Lyy on 2016/9/27.
 * 下载列表适配器
 */
public class DownloadAdapter extends AbsRVAdapter<AbsEntity, DownloadAdapter.SimpleHolder> {
  private static final String TAG = "DownloadAdapter";
  private Map<String, Integer> mPositions = new ConcurrentHashMap<>();

  public DownloadAdapter(Context context, List<AbsEntity> data) {
    super(context, data);
    int i = 0;
    for (AbsEntity entity : data) {
      mPositions.put(getKey(entity), i);
      i++;
    }
  }

  private String getKey(AbsEntity entity) {
    if (entity instanceof DownloadEntity) {
      return ((DownloadEntity) entity).getUrl();
    } else if (entity instanceof DownloadGroupEntity) {
      return ((DownloadGroupEntity) entity).getGroupName();
    }
    return "";
  }

  @Override protected SimpleHolder getViewHolder(View convertView, int viewType) {
    if (viewType == 1) return new SimpleHolder(convertView);
    if (viewType == 2) return new GroupHolder(convertView);
    return null;
  }

  public void addDownloadEntity(DownloadEntity entity) {
    mData.add(entity);
    mPositions.put(entity.getUrl(), mPositions.size());
  }

  @Override public int getItemViewType(int position) {
    AbsEntity entity = mData.get(position);
    if (entity instanceof DownloadEntity) return 1;
    if (entity instanceof DownloadGroupEntity) return 2;
    return -1;
  }

  @Override protected int setLayoutId(int type) {
    if (type == 1) {
      return R.layout.item_simple_download;
    } else if (type == 2) {
      return R.layout.item_group_download;
    }
    return android.R.layout.simple_list_item_2;
  }

  public synchronized void updateState(AbsEntity entity) {
    if (entity.getState() == IEntity.STATE_CANCEL) {
      mData.remove(entity);
      mPositions.clear();
      int i = 0;
      for (AbsEntity entity_1 : mData) {
        mPositions.put(getKey(entity_1), i);
        i++;
      }
      notifyDataSetChanged();
    } else {
      int position = indexItem(getKey(entity));
      if (position == -1 || position >= mData.size()) {
        return;
      }
      mData.set(position, entity);
      notifyItemChanged(position);
    }
  }

  /**
   * 更新进度
   */
  public synchronized void setProgress(AbsEntity entity) {
    String url = entity.getKey();
    int position = indexItem(url);
    if (position == -1 || position >= mData.size()) {
      return;
    }

    mData.set(position, entity);
    notifyItemChanged(position, entity);
  }

  private synchronized int indexItem(String url) {
    Set<String> keys = mPositions.keySet();
    for (String key : keys) {
      if (key.equals(url)) {
        return mPositions.get(key);
      }
    }
    return -1;
  }

  @Override protected void bindData(SimpleHolder holder, int position, final AbsEntity item) {
    handleProgress(holder, item);
  }

  @Override protected void bindData(SimpleHolder holder, int position, AbsEntity item,
      List<Object> payloads) {
    AbsEntity entity = (AbsEntity) payloads.get(0);
    updateSpeed(holder, entity);
  }

  /**
   * 只更新速度
   */
  private void updateSpeed(SimpleHolder holder, final AbsEntity entity) {
    long size = entity.getFileSize();
    long progress = entity.getCurrentProgress();
    int current = size == 0 ? 0 : (int) (progress * 100 / size);
    holder.speed.setText(entity.getConvertSpeed());
    holder.fileSize.setText(covertCurrentSize(progress) + "/" + CommonUtil.formatFileSize(size));
    holder.progress.setProgress(current);
    //if (holder instanceof GroupHolder){
    //  handleSubChild((GroupHolder) holder, entity);
    //}
  }

  @SuppressLint("SetTextI18n")
  private void handleProgress(SimpleHolder holder, final AbsEntity entity) {
    String str = "";
    int color = android.R.color.holo_green_light;
    switch (entity.getState()) {
      case IEntity.STATE_WAIT:
      case IEntity.STATE_OTHER:
      case IEntity.STATE_FAIL:
        str = "开始";
        break;
      case IEntity.STATE_STOP:
        str = "恢复";
        color = android.R.color.holo_blue_light;
        break;
      case IEntity.STATE_PRE:
      case IEntity.STATE_POST_PRE:
      case IEntity.STATE_RUNNING:
        str = "暂停";
        color = android.R.color.holo_red_light;
        break;
      case IEntity.STATE_COMPLETE:
        str = "重新开始？";
        holder.progress.setProgress(100);
        break;
    }
    long size = entity.getFileSize();
    long progress = entity.getCurrentProgress();
    int current = size == 0 ? 0 : (int) (progress * 100 / size);
    holder.bt.setText(str);
    holder.bt.setTextColor(getColor(color));
    holder.progress.setProgress(current);

    BtClickListener listener = new BtClickListener(entity);
    holder.bt.setOnClickListener(listener);
    String name = (isSimpleDownload(entity) ? ((DownloadEntity) entity).getFileName()
        : ((DownloadGroupEntity) entity).getAlias());
    holder.name.setText("文件名：" + name);
    holder.speed.setText(entity.getConvertSpeed());
    holder.fileSize.setText(covertCurrentSize(progress) + "/" + CommonUtil.formatFileSize(size));
    //删除按钮事件
    holder.cancel.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mData.remove(entity);
        notifyDataSetChanged();
        if (isSimpleDownload(entity)) {
          Aria.download(getContext()).load((DownloadEntity) entity).cancel();
        } else {
          Aria.download(getContext()).load((DownloadGroupEntity) entity).cancel();
        }
      }
    });
    //if (holder instanceof GroupHolder){
    //  handleSubChild((GroupHolder) holder, entity);
    //}
  }

  private void handleSubChild(GroupHolder holder, final AbsEntity entity) {
    if (holder.childList.getSubData().size() > 0) {
      holder.childList.updateChildProgress(((DownloadGroupEntity) entity).getSubTask());
    } else {
      holder.childList.addData(((DownloadGroupEntity) entity).getSubTask());
    }
  }

  private boolean isSimpleDownload(AbsEntity entity) {
    return entity instanceof DownloadEntity;
  }

  private String covertCurrentSize(long currentSize) {
    if (currentSize < 0) return "0";
    return CommonUtil.formatFileSize(currentSize);
  }

  private int getColor(int color) {
    return Resources.getSystem().getColor(color);
  }

  /**
   * 按钮事件
   */
  private class BtClickListener implements View.OnClickListener {
    private AbsEntity entity;

    BtClickListener(AbsEntity entity) {
      this.entity = entity;
    }

    @Override public void onClick(View v) {
      switch (entity.getState()) {
        case IEntity.STATE_WAIT:
        case IEntity.STATE_OTHER:
        case IEntity.STATE_FAIL:
        case IEntity.STATE_STOP:
        case IEntity.STATE_COMPLETE:
        case IEntity.STATE_PRE:
        case IEntity.STATE_POST_PRE:
          start(entity);
          break;
        case IEntity.STATE_RUNNING:
          stop(entity);
          break;
      }
    }

    private void start(AbsEntity entity) {
      if (isSimpleDownload(entity)) {
        Aria.download(getContext()).load((DownloadEntity) entity).start();
      } else {
        Aria.download(getContext()).load((DownloadGroupEntity) entity).start();
      }
    }

    private void stop(AbsEntity entity) {
      if (isSimpleDownload(entity)) {
        Aria.download(getContext()).load((DownloadEntity) entity).stop();
      } else {
        Aria.download(getContext()).load((DownloadGroupEntity) entity).stop();
      }
    }
  }

  class SimpleHolder extends AbsHolder {
    @Bind(R.id.progressBar) HorizontalProgressBarWithNumber progress;
    @Bind(R.id.bt) Button bt;
    @Bind(R.id.speed) TextView speed;
    @Bind(R.id.fileSize) TextView fileSize;
    @Bind(R.id.del) TextView cancel;
    @Bind(R.id.name) TextView name;

    SimpleHolder(View itemView) {
      super(itemView);
    }
  }

  class GroupHolder extends SimpleHolder {
    @Bind(R.id.child_list) SubStateLinearLayout childList;

    GroupHolder(View itemView) {
      super(itemView);
    }
  }
}