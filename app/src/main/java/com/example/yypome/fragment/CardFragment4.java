package com.example.yypome.fragment;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yypome.R;
import com.example.yypome.adapter.MethodAdapter;
import com.example.yypome.db.PoemRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CardFragment4#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardFragment4 extends Fragment {

    private View rootView;

    RecyclerView recyclerView;
    private ImageView homeButton;
    private ImageView toggleButton;
    private boolean isListVisible = false; // 列表是否可见

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "title";
    private static final String ARG_PARAM2 = "original_text";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private List<String> methodList = Arrays.asList(
            "联想记忆法", "关键词记忆法", "分类记忆法", "规律记忆法", "挖空记忆法", "提取观点记忆法"
//            "问题记忆法", "首字串联背诵法", "理解文意法", "化整为零法", "反复诵读记忆法"
    );

    public CardFragment4() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CardFragment4.
     */
    // TODO: Rename and change types and number of parameters
    public static CardFragment4 newInstance(String param1, String param2) {
        CardFragment4 fragment = new CardFragment4();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_card4, container, false);

        // 在后台线程中获取 suggestMethodMap 数据
        new AsyncTask<Void, Void, Map<String, String>>() {
            @Override
            protected Map<String, String> doInBackground(Void... voids) {
                PoemRepository poemRepository = new PoemRepository(getContext());
                return poemRepository.getSuggestMethodMapSync();
            }

            @Override
            protected void onPostExecute(Map<String, String> suggestMethodMap) {
                setupRecyclerView(suggestMethodMap);
            }
        }.execute();

        // 初始化展开/折叠按钮
        toggleButton = rootView.findViewById(R.id.btn_toggle_list);
        toggleButton.setOnClickListener(v -> toggleMethodList());

        // 初始化 ic_home 图标按钮
        homeButton = rootView.findViewById(R.id.ic_home);
        homeButton.setOnClickListener(v -> openHomeFragment());

        // 一开始默认界面
        openHomeFragment();

        return rootView;
    }

    private void setupRecyclerView(Map<String, String> suggestMethodMap) {
        recyclerView = rootView.findViewById(R.id.rv_method_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        MethodAdapter adapter = new MethodAdapter(getContext(), methodList, mParam1, suggestMethodMap, this::openMethodFragment);
        recyclerView.setAdapter(adapter);
    }

    // 控制列表的显示和隐藏
    private void toggleMethodList() {
        if (isListVisible) {
            // 列表可见，折叠列表
            recyclerView.setVisibility(View.GONE);
            toggleButton.setImageResource(R.drawable.baseline_keyboard_arrow_right_24);
        } else {
            // 列表不可见，展开列表
            recyclerView.setVisibility(View.VISIBLE);
            toggleButton.setImageResource(R.drawable.baseline_keyboard_arrow_down_24);
        }
        isListVisible = !isListVisible; // 切换状态
    }

    private void openMethodFragment(String methodName) {
        Fragment fragment = null;

        switch (methodName) {
            case "联想记忆法":
                fragment = MethodFragmentAssociation.newInstance(mParam1, mParam2);
                break;
            case "关键词记忆法":
                fragment = MethodFragmentKeyword.newInstance(mParam1, mParam2);
                break;
            // 根据不同的方法名跳转到不同的Fragment
            case "分类记忆法":
                fragment = MethodFragmentCategorization.newInstance(mParam1, mParam2); // 假设你有这个Fragment
                break;
            case "规律记忆法":
                fragment = MethodFragmentRule.newInstance(mParam1, mParam2); // 假设你有这个Fragment
                break;
            case "挖空记忆法":
                fragment = MethodFragmentOcclusion.newInstance(mParam1, mParam2);
                break;
            case "提取观点记忆法":
                fragment = MethodFragmentExtractIdeas.newInstance(mParam1, mParam2);
                break;
            // 添加其他方法的Fragment
            default:
//                Toast.makeText(getContext(), "功能尚未开发", Toast.LENGTH_SHORT).show();
                MyToastShow(R.drawable.toast_pic2, "功能尚未开发️🚵‍♂️");
        }

        if (fragment != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.method_fragment_container, fragment)
                    .commit();
//                    .addToBackStack(null)

        }
    }

    private void MyToastShow(int iconResId, String message) {
        // 获取布局服务
        LayoutInflater inflater = getLayoutInflater();
        // 使用自定义布局
        View layout = inflater.inflate(R.layout.custom_toast, null);

        // 设置图标和文本
        ImageView toastIcon = layout.findViewById(R.id.toast_icon);
        toastIcon.setImageResource(iconResId); // 设置你想要的图标

        TextView toastText = layout.findViewById(R.id.toast_message);
        toastText.setText(message);

        // 创建并显示自定义的Toast
        Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    // 打开Home Fragment，当点击 ic_home 图标时调用
    private void openHomeFragment() {
        Fragment homeFragment = new MethodFragmentHome(); // 假设你有这个Fragment

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.method_fragment_container, homeFragment)
                .commit();
    }
}