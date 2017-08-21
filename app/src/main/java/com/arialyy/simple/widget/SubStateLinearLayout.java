package com.arialyy.simple.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.simple.R;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Aria.Lao on 2017/7/17.
 */
public class SubStateLinearLayout extends LinearLayout {

  interface OnShowCallback {
    void onShow(boolean visibility);
  }

  OnShowCallback callback;

  List<DownloadEntity> mSubData = new LinkedList<>();
  Map<String, Integer> mPosition = new WeakHashMap<>();

  public SubStateLinearLayout(Context context) {
    super(context);
    setOrientation(VERTICAL);
  }

  public SubStateLinearLayout(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setOrientation(VERTICAL);
  }

  public SubStateLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setOrientation(VERTICAL);
  }

  public void addData(List<DownloadEntity> datas) {
    removeAllViews();
    mSubData.clear();
    mSubData.addAll(datas);
    createShowView();
    int i = 1;
    for (DownloadEntity entity : datas) {
      TextView view = createView(entity);
      mPosition.put(entity.getDownloadPath(), i);
      addView(view, i);
      i++;
    }
  }

  public void setOnShowCallback(OnShowCallback callback) {
    this.callback = callback;
  }

  public List<DownloadEntity> getSubData() {
    return mSubData;
  }

  public void updateChildProgress(List<DownloadEntity> entities) {
    for (DownloadEntity entity : entities) {
      Integer i = mPosition.get(entity.getDownloadPath());
      if (i == null) return;
      int position = i;
      if (position != -1) {
        TextView child = ((TextView) getChildAt(position));
        int p = getPercent(entity);
        child.setText(entity.getFileName() + ": " + p + "%" + "   | " + entity.getConvertSpeed());
        child.invalidate();
      }
    }
  }

  private TextView createView(DownloadEntity entity) {
    TextView view =
        (TextView) LayoutInflater.from(getContext()).inflate(R.layout.layout_child_state, null);
    view.setText(entity.getFileName() + ": " + getPercent(entity) + "%");
    return view;
  }

  private void createShowView() {

    TextView view =
        (TextView) LayoutInflater.from(getContext()).inflate(R.layout.layout_child_state, null);
    view.setText("点击显示子任务");
    view.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        int visibility = getChildAt(1).getVisibility();
        if (visibility == GONE) {
          showChild(true);
          ((TextView) v).setText("点击隐藏子任务");
        } else {
          showChild(false);
          ((TextView) v).setText("点击显示子任务");
        }
      }
    });
    addView(view, 0);
  }

  private void showChild(boolean show) {
    for (int i = 1, count = getChildCount(); i < count; i++) {
      getChildAt(i).setVisibility(show ? VISIBLE : GONE);
      invalidate();
    }
  }

  private int getPercent(DownloadEntity entity) {
    long size = entity.getFileSize();
    long progress = entity.getCurrentProgress();
    return size == 0 ? 0 : (int) (progress * 100 / size);
  }
}
