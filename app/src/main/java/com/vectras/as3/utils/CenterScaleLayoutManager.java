package com.vectras.as3.utils;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class CenterScaleLayoutManager extends LinearLayoutManager {

    private final float scaleFactor;

    public CenterScaleLayoutManager(Context context, float scaleFactor) {
        super(context);
        this.scaleFactor = scaleFactor;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        scaleChildren();
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        scaleChildren();
    }

    private void scaleChildren() {
        int center = getWidth() / 2;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int childCenter = (child.getLeft() + child.getRight()) / 2;
            float scale = Math.max(0.5f, 1 - (Math.abs(childCenter - center) / (float) center) * (1 - scaleFactor));
            child.setScaleX(scale);
            child.setScaleY(scale);
        }
    }
}