/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.linzhenbin.xjxxxjjjkkkyyyyttt.player.ui.adapter;




import com.linzhenbin.xjxxxjjjkkkyyyyttt.ResourceTable;
import ohos.agp.components.*;
import ohos.app.Context;

import java.util.List;

public class TestListItemProvider extends RecycleItemProvider {
    private static final String TAG = "TestListItemProvider";

    private Context mContext;

    private List<String> itemLists;

    public TestListItemProvider(Context context, List<String> itemList) {
        this.mContext = context;
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
        Component rootView = LayoutScatter.getInstance(mContext)
                .parse(ResourceTable.Layout_list_item_test1, null, false);

        String str = itemLists.get(position);
        Text item_name = (Text) rootView.findComponentById(ResourceTable.Id_item_name_test);
        item_name.setText(str);
        return rootView;
    }

}