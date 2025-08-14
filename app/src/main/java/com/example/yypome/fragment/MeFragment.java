package com.example.yypome.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yypome.R;
import com.example.yypome.adapter.MeListAdapter;
import com.example.yypome.data.MeListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeFragment extends Fragment {

    private View rootView;
    private RecyclerView recyclerView;
    private MeListAdapter listAdapter;
    private List<MeListItem> listItems;

    public MeFragment() {
        // Required empty public constructor
    }

    public static MeFragment newInstance(String param1, String param2) {
        MeFragment fragment = new MeFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_me, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 初始化数据
        listItems = new ArrayList<>();
        listItems.add(new MeListItem(R.drawable.ic_wallet, "我的钱包"));
        listItems.add(new MeListItem(R.drawable.ic_member, "我的会员"));
        listItems.add(new MeListItem(R.drawable.ic_poetry, "我的诗单"));
        listItems.add(new MeListItem(R.drawable.ic_mistakes, "我的错题"));
        listItems.add(new MeListItem(R.drawable.ic_sign, "我的签到"));
        listItems.add(new MeListItem(R.drawable.ic_level, "我的等级"));
        listItems.add(new MeListItem(R.drawable.ic_follow, "我的关注"));
        listItems.add(new MeListItem(R.drawable.ic_settings, "设置"));
        listItems.add(new MeListItem(R.drawable.ic_feedback, "问题反馈"));
        listItems.add(new MeListItem(R.drawable.ic_changeuser, "切换账号"));

        // 设置适配器
        listAdapter = new MeListAdapter(getActivity(), listItems);
        recyclerView.setAdapter(listAdapter);

        return rootView;
    }
}