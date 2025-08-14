package com.example.yypome.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.yypome.studytopfragment.SubFragment1;
import com.example.yypome.studytopfragment.SubFragment2;

public class ViewPager2Adapter extends FragmentStateAdapter {

    private String[] titles;

    public ViewPager2Adapter(@NonNull Fragment fragment, String[] titles) {
        super(fragment);
        this.titles = titles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String title = titles[position];
        if (position == 0) {
            SubFragment1 subFragment = SubFragment1.newInstance(title);
            return subFragment;
        } else if (position == 1) {
            SubFragment2 subFragment = SubFragment2.newInstance(title);
            return subFragment;
        } else {
            // 其他情况
            SubFragment1 subFragment = SubFragment1.newInstance(title);
            return subFragment;
        }
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}

