package com.willowtreeapps.hyperion.core.internal;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.willowtreeapps.hyperion.R;
import com.willowtreeapps.hyperion.core.plugins.DrawerContainer;
import com.willowtreeapps.hyperion.core.plugins.PluginExtension;
import com.willowtreeapps.hyperion.core.plugins.PluginModule;

import java.util.List;

import javax.inject.Inject;

public class HyperionPluginView extends FrameLayout implements DrawerContainer {

    private final LinearLayout pluginListContainer;
    private final PluginExtension pluginExtension;

    @Inject
    PluginLoader pluginLoader;

    private List<PluginModule> modules;

    public HyperionPluginView(@NonNull Context context) {
        this(context, null);
    }

    public HyperionPluginView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HyperionPluginView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        CoreComponent component = ComponentProvider.get(context);
        component.inject(this);
        inflate(context, R.layout.hype_view_plugin, this);

        setBackgroundColor(ContextCompat.getColor(context, R.color.hype_white));
        pluginListContainer = (LinearLayout) findViewById(R.id.plugin_list_container);
        pluginExtension = new PluginExtensionImpl(component, this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        pluginLoader.load(new Callback<List<PluginModule>>() {
            @Override
            public void call(Try<List<PluginModule>> result) {
                if (ViewCompat.isAttachedToWindow(HyperionPluginView.this)) {
                    update(result);
                }
            }
        });
    }

    private void update(Try<List<PluginModule>> result) {

        try {
            modules = result.get();

        } catch (Throwable t) {
            // TODO
            t.printStackTrace();
            return;
        }

        final Context context = new PluginExtensionContextWrapper(
                getContext(), pluginExtension);
        final LayoutInflater inflater = LayoutInflater.from(context);
        for (PluginModule module : modules) {
            module.create(pluginExtension, context);
            View view = module.createPluginView(inflater, pluginListContainer);
            pluginListContainer.addView(view);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (modules != null) {
            for (PluginModule module : modules) {
                module.destroy();
            }
        }
    }

    @Override
    public void addView(@LayoutRes int view) {
        // TODO
    }
}