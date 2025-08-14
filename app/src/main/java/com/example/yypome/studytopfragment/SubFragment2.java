package com.example.yypome.studytopfragment;

import android.os.Bundle;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.yypome.R;
import com.example.yypome.fragment.CheckFragment;
import com.example.yypome.fragment.MeFragment;
import com.example.yypome.fragment.ReviewFragment;
import com.example.yypome.fragment.StudyFragment;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class SubFragment2 extends Fragment {

    private View rootView;
    private SubFragment21 subFragment21;
    private NestedScrollView nestedScrollView;  // 引用 NestedScrollView

    private static final String ARG_PARAM1 = "title";

    private String title;
    private String TAG = "SubFragment2";

    public SubFragment2() {
        // Required empty public constructor
    }

    public static SubFragment2 newInstance(String title) {
        SubFragment2 fragment = new SubFragment2();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_sub2, container, false);

        LinearLayout btn_style1 = rootView.findViewById(R.id.style1);
        LinearLayout btn_style2 = rootView.findViewById(R.id.style2);
        LinearLayout btn_style3 = rootView.findViewById(R.id.style3);
        LinearLayout btn_style4 = rootView.findViewById(R.id.style4);
        LinearLayout btn_style5 = rootView.findViewById(R.id.style5);

//        TabItem ap_style1 = rootView.findViewById(R.id.ap_style1);
//        TabItem ap_style2 = rootView.findViewById(R.id.ap_style2);
//        TabItem ap_style3 = rootView.findViewById(R.id.ap_style3);
//        TabItem ap_style4 = rootView.findViewById(R.id.ap_style4);
//        TabItem ap_style5 = rootView.findViewById(R.id.ap_style5);

        TabLayout tabLayout = rootView.findViewById(R.id.tabLayout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                // 根据 position 执行不同的操作
                if (position == 0) {
                    // ap_style1 选中时的操作
                    selectedApproachFragment(0);
                } else if (position == 1) {
                    // ap_style2 选中时的操作
                    selectedApproachFragment(1);
                } else if (position == 2) {
                    // ap_style3 选中时的操作
                    selectedApproachFragment(2);
                } else if (position == 3) {
                    // ap_style4 选中时的操作
                    selectedApproachFragment(3);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 初始化 NestedScrollView
        nestedScrollView = rootView.findViewById(R.id.nestedScrollView);

        btn_style5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFragment(4);
                // 替换当前 Fragment 并将事务添加到回退栈
            }
        });

        btn_style1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFragment(0);
                // 替换当前 Fragment 并将事务添加到回退栈
            }
        });

        btn_style2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFragment(1);
                // 替换当前 Fragment 并将事务添加到回退栈
            }
        });

        btn_style3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFragment(2);
                // 替换当前 Fragment 并将事务添加到回退栈
            }
        });

        btn_style4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFragment(3);
                // 替换当前 Fragment 并将事务添加到回退栈
            }
        });

//        ap_style1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                selectedApproachFragment(0);
//                // 替换当前 Fragment 并将事务添加到回退栈
//            }
//        });
//
//        ap_style2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                selectedApproachFragment(1);
//                // 替换当前 Fragment 并将事务添加到回退栈
//            }
//        });
//
//        ap_style3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                selectedApproachFragment(2);
//                // 替换当前 Fragment 并将事务添加到回退栈
//            }
//        });
//
//        ap_style4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                selectedApproachFragment(3);
//                // 替换当前 Fragment 并将事务添加到回退栈
//            }
//        });
//
//        ap_style5.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                selectedApproachFragment(4);
//                // 替换当前 Fragment 并将事务添加到回退栈
//            }
//        });


        // 监听回退栈的变化
        getChildFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                // 如果回退栈为空，显示 NestedScrollView
                if (getChildFragmentManager().getBackStackEntryCount() == 0) {
                    Log.d(TAG, "onBackStackChanged: 监听到pop");
                    // 显示 NestedScrollView 并确保移除 subFragment21
                    nestedScrollView.setVisibility(View.VISIBLE);
                }
            }
        });

        return rootView;
    }

    private void selectedApproachFragment(int position) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
//        hideFragment(fragmentTransaction);

        if (position == 0) {
            subFragment21 = SubFragment21.newInstance("文言文", "style");
            fragmentTransaction.replace(R.id.sub2_container, subFragment21, "SubFragment27");
        } else if(position == 1) {
            subFragment21 = SubFragment21.newInstance("诗", "style");
            fragmentTransaction.replace(R.id.sub2_container, subFragment21, "SubFragment28");
        } else if(position == 2) {
            subFragment21 = SubFragment21.newInstance("词", "style");
            fragmentTransaction.replace(R.id.sub2_container, subFragment21, "SubFragment29");
        } else {
            subFragment21 = SubFragment21.newInstance("曲", "style");
            fragmentTransaction.replace(R.id.sub2_container, subFragment21, "SubFragment2p");
        }

        // 隐藏 NestedScrollView
        nestedScrollView.setVisibility(View.GONE);

        // 添加到回退栈，以便在返回时恢复 NestedScrollVie
        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();

        // 一定要提交
        fragmentTransaction.commit();
    }

    private void selectedFragment(int position) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
//        hideFragment(fragmentTransaction);

        if (position == 0) {
            subFragment21 = SubFragment21.newInstance("托物言志", "approach");
            fragmentTransaction.replace(R.id.sub2_container, subFragment21, "SubFragment21");
        } else if (position == 1) {
            subFragment21 = SubFragment21.newInstance("借景抒情", "approach");
            fragmentTransaction.replace(R.id.sub2_container, subFragment21, "SubFragment22");
        } else if(position == 2) {
            subFragment21 = SubFragment21.newInstance("怀古伤今", "approach");
            fragmentTransaction.replace(R.id.sub2_container, subFragment21, "SubFragment23");
        } else if(position == 3) {
            subFragment21 = SubFragment21.newInstance("直抒胸臆", "approach");
            fragmentTransaction.replace(R.id.sub2_container, subFragment21, "SubFragment24");
        } else {
            subFragment21 = SubFragment21.newInstance("其他", "approach");
            fragmentTransaction.replace(R.id.sub2_container, subFragment21, "SubFragment25");
        }

        // 隐藏 NestedScrollView
        nestedScrollView.setVisibility(View.GONE);

        // 添加到回退栈，以便在返回时恢复 NestedScrollVie
        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();

        // 一定要提交
        fragmentTransaction.commit();

    }



}

