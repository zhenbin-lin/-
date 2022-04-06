/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.linzhenbin.xjxxxjjjkkkyyyyttt.player.ui.adapter;



import com.linzhenbin.xjxxxjjjkkkyyyyttt.ResourceTable;
import com.linzhenbin.xjxxxjjjkkkyyyyttt.player.VideoAbility;
import ohos.agp.components.*;

import java.util.List;

/**
 * HudListItemProvider extends BaseItemProvider
 */
public class HudListItemProvider extends RecycleItemProvider {
    private VideoAbility abilitySlice;

    private List<SampleItem> itemLists;

    public HudListItemProvider(VideoAbility currentAbilitySlice, List<SampleItem> itemList) {
        this.abilitySlice = currentAbilitySlice;
        this.itemLists = itemList;
    }

    @Override
    public int getCount() {
        return itemLists == null ? 0 : itemLists.size();
    }

    @Override
    public Object getItem(int position) {

        return itemLists == null ? null : itemLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Component getComponent(int position, Component component, ComponentContainer componentContainer) {
        return getRootView(position);
    }

    private Component getRootView(int position) {
        Component rootView = LayoutScatter.getInstance(abilitySlice)
                .parse(ResourceTable.Layout_list_item_hud_view, null, false);
        SampleItem sampleItem = itemLists.get(position);
        Text item_key = (Text) rootView.findComponentById(ResourceTable.Id_item_key);
        Text item_value = (Text) rootView.findComponentById(ResourceTable.Id_item_value);
        item_key.setText(sampleItem.key);
        item_value.setText(sampleItem.value);
        return rootView;
    }

    public void setData(List<SampleItem> newDataLists) {
      itemLists.containsAll(newDataLists);
        notifyDataChanged();
    }
}