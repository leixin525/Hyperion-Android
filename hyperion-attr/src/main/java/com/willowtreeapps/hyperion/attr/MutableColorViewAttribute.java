package com.willowtreeapps.hyperion.attr;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.willowtreeapps.hyperion.attr.collectors.ColorValue;

public abstract class MutableColorViewAttribute extends MutableViewAttribute<ColorValue> {

    public MutableColorViewAttribute(String key, @NonNull ColorValue value) {
        this(key, value, new ColorDrawable(value.getColor()));

    }

    MutableColorViewAttribute(String key, @NonNull ColorValue value, @Nullable Drawable drawable) {
        super(key, value, drawable);
    }

    @Override
    public int getViewType() {
        return AttributeDetailView.ITEM_MUTABLE_COLOR_ATTRIBUTE;
    }
}