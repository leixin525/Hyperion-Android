package com.willowtreeapps.hyperion.attr;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public abstract class MutableViewAttribute<T> extends ViewAttribute<T> {

    private boolean activated;

    MutableViewAttribute(String key, @NonNull T value) {
        super(key, value);
    }

    MutableViewAttribute(String key, @NonNull T value, @Nullable Drawable drawable) {
        super(key, value, drawable);
    }

    void setValue(T value) {
        try {
            mutate(value);
            this.value = value;
        } catch (Exception e) {
            Log.e("Hyperion", "Error mutating view", e);
        }
    }

    protected abstract void mutate(T value) throws Exception;

    boolean isActivated() {
        return this.activated;
    }

    void setActivated(boolean activated) {
        this.activated = activated;
    }
}