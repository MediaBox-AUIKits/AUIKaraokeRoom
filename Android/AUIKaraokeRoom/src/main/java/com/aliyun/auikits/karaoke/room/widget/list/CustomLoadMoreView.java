package com.aliyun.auikits.karaoke.room.widget.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.auikits.ktv.R;
import com.chad.library.adapter.base.loadmore.BaseLoadMoreView;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;

public class CustomLoadMoreView extends BaseLoadMoreView {
    @NotNull
    @Override
    public View getRootView(@NotNull ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.ktv_loading_more_view, parent, false);
    }

    @NotNull
    @Override
    public View getLoadingView(@NotNull BaseViewHolder holder) {
        return holder.findView(R.id.load_more_loading_view);
    }

    @NotNull
    @Override
    public View getLoadComplete(@NotNull BaseViewHolder holder) {
        return holder.findView(R.id.load_more_load_complete_view);
    }

    @NotNull
    @Override
    public View getLoadEndView(@NotNull BaseViewHolder holder) {
        return holder.findView(R.id.load_more_load_end_view);
    }

    @NotNull
    @Override
    public View getLoadFailView(@NotNull BaseViewHolder holder) {
        return holder.findView(R.id.load_more_load_fail_view);
    }

}
