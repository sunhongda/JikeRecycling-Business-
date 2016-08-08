package com.angm.jikeb.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.angm.jikeb.R;

/**
 * Created by shd on 16-8-4.
 * 用于地图Fragment(首页ViewPager)
 */
public class MapFragment extends LevelTwoFragment {
    public MapFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public String getTitle() {
        return "地图";
    }

    @Override
    protected int getResLayout() {
        return R.layout.fragment_map;
    }

    @Override
    protected void initView() {

    }


}
