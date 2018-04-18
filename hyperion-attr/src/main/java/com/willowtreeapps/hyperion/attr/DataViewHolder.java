package com.willowtreeapps.hyperion.attr;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

abstract class DataViewHolder<T> extends RecyclerView.ViewHolder {

    private T data;
    final Context context;

    public DataViewHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
    }

    protected T getData() {
        return this.data;
    }

    void bind(T data) {
        this.data = data;
        onDataChanged(data);
    }

    void onDataChanged(T data) {

    }
}