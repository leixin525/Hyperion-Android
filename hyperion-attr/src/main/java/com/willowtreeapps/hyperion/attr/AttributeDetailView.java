package com.willowtreeapps.hyperion.attr;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.willowtreeapps.hyperion.attr.collectors.ColorValue;
import com.willowtreeapps.hyperion.plugin.v1.ExtensionProvider;
import com.willowtreeapps.hyperion.plugin.v1.PluginExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 读取控件属性信息，并展示
 */
class AttributeDetailView extends RecyclerView {

    static final int ITEM_HEADER = 1;
    static final int ITEM_ATTRIBUTE = 2;
    static final int ITEM_MUTABLE_COLOR_ATTRIBUTE = 3;
    static final int ITEM_MUTABLE_STRING_ATTRIBUTE = 4;
    static final int ITEM_MUTABLE_BOOLEAN_ATTRIBUTE = 5;

    private final AttributeAdapter adapter;
    private final AttributeLoader attributeLoader;

    public AttributeDetailView(@NonNull Context context) {
        this(context, null);
    }

    public AttributeDetailView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AttributeDetailView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        PluginExtension extension = ExtensionProvider.get(context);
        adapter = new AttributeAdapter();
        attributeLoader = new AttributeLoader(extension.getAttributeTranslator());

        setHasFixedSize(true);
        setLayoutManager(new LinearLayoutManager(context));
        setAdapter(adapter);
        DividerItemDecoration decoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        //noinspection ConstantConditions
        decoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.ha_detail_divider));
        addItemDecoration(decoration);
    }

    public void setTarget(View target) {
        if (target != null) {
            List<Section<ViewAttribute>> sections = attributeLoader.getAttributesForView(target);
            List<AttributeDetailItem> items = toItems(sections);
            adapter.setItems(items);
        } else {
            adapter.setItems(Collections.<AttributeDetailItem>emptyList());
        }
    }

    private List<AttributeDetailItem> toItems(List<Section<ViewAttribute>> sections) {
        List<AttributeDetailItem> items = new ArrayList<>(18);
        for (Section<ViewAttribute> section : sections) {
            items.add(new Header(section.getName()));
            items.addAll(section.getList());
        }
        return items;
    }

    private static class AttributeAdapter extends RecyclerView.Adapter<DataViewHolder> {

        private List<AttributeDetailItem> items;

        private void setItems(List<AttributeDetailItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        @NonNull
        public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView;
            switch (viewType) {
                case ITEM_HEADER:
                    itemView = inflater.inflate(R.layout.ha_item_header, parent, false);
                    return new HeaderViewHolder(itemView);
                case ITEM_ATTRIBUTE:
                    itemView = inflater.inflate(R.layout.ha_item_attribute, parent, false);
                    return new AttributeViewHolder(itemView);
                case ITEM_MUTABLE_COLOR_ATTRIBUTE:
                    itemView = inflater.inflate(R.layout.ha_item_mutable_color_attribute, parent, false);
                    return new MutableColorAttributeViewHoler(itemView);
                case ITEM_MUTABLE_STRING_ATTRIBUTE:
                    itemView = inflater.inflate(R.layout.ha_item_mutable_string_attribute, parent, false);
                    return new MutableStringAttributeViewHolder(itemView);
                case ITEM_MUTABLE_BOOLEAN_ATTRIBUTE:
                    itemView = inflater.inflate(R.layout.ha_item_mutable_boolean_attribute, parent, false);
                    return new MutableBooleanAttributeViewHolder(itemView);
                default:
                    throw new IllegalStateException("Did not recognize view type: " + viewType);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
            //noinspection unchecked
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).getViewType();
        }
    }

    private static class HeaderViewHolder extends DataViewHolder<Header> {

        private final TextView text;

        private HeaderViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }

        @Override
        void onDataChanged(Header data) {
            text.setText(data.getText());
        }
    }

    private static class AttributeViewHolder<T extends ViewAttribute> extends DataViewHolder<T> {

        private final TextView keyText;
        final TextView valueText;
        final ImageView image;

        private AttributeViewHolder(View itemView) {
            super(itemView);
            keyText = itemView.findViewById(R.id.key_text);
            valueText = itemView.findViewById(R.id.value_text);
            image = itemView.findViewById(R.id.image);
        }

        @Override
        void onDataChanged(ViewAttribute data) {
            keyText.setText(data.getKey());

            valueText.setText(data.getDisplayValue());

            Drawable drawable = data.getDrawable();
            if (drawable != null) {
                image.setImageDrawable(drawable);
                image.setVisibility(View.VISIBLE);
            } else {
                image.setVisibility(View.GONE);
            }
        }
    }

    private static class MutableColorAttributeViewHoler
            extends AttributeViewHolder<MutableColorViewAttribute>
            implements OnClickListener, TextWatcher, ExpandableLayout.OnExpansionUpdateListener {

        private final ExpandableLayout detail;
        private final EditText editText;
        private final ImageView imageColor;

        private MutableColorAttributeViewHoler(View itemView) {
            super(itemView);
            detail = itemView.findViewById(R.id.detail);
            editText = itemView.findViewById(R.id.edit_text);
            imageColor = itemView.findViewById(R.id.image_color);
            itemView.setOnClickListener(this);
            detail.setOnExpansionUpdateListener(this);
        }

        @Override
        void onDataChanged(MutableColorViewAttribute data) {
            super.onDataChanged(data);
            final ColorValue colorValue = data.value;
            boolean activated = data.isActivated();
            itemView.setActivated(activated);
            detail.setExpanded(activated, false);
            loadColorValue(colorValue);
        }

        @Override
        public void afterTextChanged(Editable s) {
            final MutableColorViewAttribute attribute = getData();
            try {
                final String value = s.toString();
                final ColorValue colorValue = new ColorValue(Color.parseColor(value));
                attribute.setValue(new ColorValue(Color.parseColor(value)));
                valueText.setText(colorValue.getDisplayValue());
                valueText.setVisibility(View.VISIBLE);
                final ColorDrawable colorDrawable = new ColorDrawable(colorValue.getColor());
                imageColor.setImageDrawable(colorDrawable);
                attribute.setValue(colorValue);
            } catch (Exception ignore) {
            }
        }

        @Override
        public void onClick(View v) {
            final MutableColorViewAttribute attribute = getData();
            boolean activated = !attribute.isActivated();
            attribute.setActivated(activated);
            itemView.setActivated(activated);
            detail.setExpanded(activated);
        }

        @Override
        public void onExpansionUpdate(float expansionFraction, int state) {
            if (state == ExpandableLayout.State.COLLAPSED) {
                loadColorValue(getData().value);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        private void loadColorValue(@Nullable ColorValue colorValue) {
            if (colorValue == null) {
                return;
            }
            editText.removeTextChangedListener(this);
            editText.setText(colorValue.getDisplayValue());
            editText.addTextChangedListener(this);
            imageColor.setImageDrawable(new ColorDrawable(colorValue.getColor()));
            image.setImageDrawable(imageColor.getDrawable());
        }


    }

    private static class MutableStringAttributeViewHolder
            extends AttributeViewHolder<MutableStringViewAttribute>
            implements OnClickListener, TextWatcher {

        private final ExpandableLayout detail;
        private final EditText editText;

        private MutableStringAttributeViewHolder(View itemView) {
            super(itemView);
            detail = itemView.findViewById(R.id.detail);
            editText = itemView.findViewById(R.id.edit_text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final MutableStringViewAttribute attribute = getData();
            boolean activated = !attribute.isActivated();
            attribute.setActivated(activated);
            itemView.setActivated(activated);
            detail.setExpanded(activated);
        }

        @Override
        void onDataChanged(MutableStringViewAttribute data) {
            super.onDataChanged(data);
            boolean activated = data.isActivated();
            itemView.setActivated(activated);
            detail.setExpanded(activated, false);
            editText.removeTextChangedListener(this);
            editText.setText(data.getDisplayValue());
            editText.addTextChangedListener(this);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            final MutableStringViewAttribute attribute = getData();
            final String value = s.toString();
            attribute.setValue(value);
            valueText.setText(value);
            valueText.setVisibility(View.VISIBLE);
        }
    }

    private static class MutableBooleanAttributeViewHolder
            extends DataViewHolder<MutableBooleanViewAttribute> implements OnClickListener {

        private final TextView keyText;
        private final Switch booleanSwitch;

        private MutableBooleanAttributeViewHolder(View itemView) {
            super(itemView);
            keyText = itemView.findViewById(R.id.key_text);
            booleanSwitch = itemView.findViewById(R.id.boolean_switch);
            booleanSwitch.setOnClickListener(this);
        }

        @Override
        void onDataChanged(MutableBooleanViewAttribute data) {
            keyText.setText(data.getKey());
            booleanSwitch.setChecked(data.getBoolean());
        }

        @Override
        public void onClick(View v) {
            final MutableBooleanViewAttribute attribute = getData();
            attribute.setValue(booleanSwitch.isChecked());
        }
    }
}