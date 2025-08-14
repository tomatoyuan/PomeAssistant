package com.example.yypome.studytopfragment;

import android.media.Image;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.yypome.R;
import com.example.yypome.adapter.MyAdapter;
import com.example.yypome.data.CardData;
import com.example.yypome.db.PoemRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SubFragment1 extends Fragment {

    private View rootView;
    RecyclerView recyclerView;
    List<CardData> cardDataList = new ArrayList<>();  // 初始化为空列表

    private ListView searchResultsList;
    private ArrayAdapter<String> searchAdapter;
    private List<String> searchResults = new ArrayList<>();

    private static final String ARG_PARAM1 = "title";

    private String title;
    private String TAG = "SubFragment1";

    public SubFragment1() {
        // Required empty public constructor
    }

    public static SubFragment1 newInstance(String title) {
        SubFragment1 fragment = new SubFragment1();
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
        rootView = inflater.inflate(R.layout.fragment_sub1, container, false);

        // 初始化控件
        recyclerView = rootView.findViewById(R.id.recyclerView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 初始化适配器
        MyAdapter myAdapter = new MyAdapter(getActivity(), cardDataList);

        // 设置adapter
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));

        // 通过 Repository 从数据库获取数据并观察变化
        PoemRepository repository = new PoemRepository(getActivity());
        repository.getAllCardData().observe(getViewLifecycleOwner(), new Observer<List<CardData>>() {
            @Override
            public void onChanged(List<CardData> cardData) {
                // 当数据库数据发生变化时更新列表
                cardDataList.clear();
                cardDataList.addAll(cardData);
                myAdapter.notifyDataSetChanged();
            }
        });

        // 设置搜索功能
        setupSearchFunctionality();
    }

    private void setupSearchFunctionality() {
        // 设置搜索标题，跳转对应卡片功能
        EditText searchEditText = rootView.findViewById(R.id.search_edit_text);
        searchResultsList = rootView.findViewById(R.id.search_results_list);
        ImageView imgSearch = rootView.findViewById(R.id.img_search);

        searchAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, searchResults);
        searchResultsList.setAdapter(searchAdapter);

        // 监听搜索框变化
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString().toLowerCase();
                searchResults.clear();

                findCardsEdit(query);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // 点击搜索
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = searchEditText.getText().toString().toLowerCase();
                searchResults.clear();

                findCards(query);
            }
        });

        searchResultsList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTitle = searchResults.get(position);
            if (!selectedTitle.equals("没有找到😣")) {
                filterCards(selectedTitle);
            }

            searchResultsList.setVisibility(View.GONE); // 点击后隐藏结果
        });
    }

    private void findCardsEdit(String query) {
        for (CardData cardData : cardDataList) {
            if (cardData.getTitle().toLowerCase().contains(query) && !query.isEmpty()) {
                searchResults.add(cardData.getTitle());
            }
        }

        if (query.isEmpty()) {
            searchResultsList.setVisibility(View.GONE);
        } else {
            if (searchResults.isEmpty()) {
                searchResultsList.setVisibility(View.VISIBLE);
                searchResults.add("没有找到😣");
            } else {
                searchResultsList.setVisibility(View.VISIBLE);
            }
        }

        searchAdapter.notifyDataSetChanged();
    }

    private void findCards(String query) {
        for (CardData cardData : cardDataList) {
            if (cardData.getTitle().toLowerCase().contains(query) && !query.isEmpty()) {
                searchResults.add(cardData.getTitle());
            }
        }

        if (query.isEmpty()) {
            Toast.makeText(getContext(), "请输入标题！", Toast.LENGTH_SHORT).show();
            searchResultsList.setVisibility(View.GONE);
        } else {
            if (searchResults.isEmpty()) {
                searchResultsList.setVisibility(View.VISIBLE);
                searchResults.add("没有找到😣");
            } else {
                searchResultsList.setVisibility(View.VISIBLE);
            }
        }

        searchAdapter.notifyDataSetChanged();
    }

    private void filterCards(String query) {

        for (int i = 0; i < cardDataList.size(); i++) {
            if (cardDataList.get(i).getTitle().toLowerCase().contains(query.toLowerCase())) {
//                recyclerView.smoothScrollToPosition(i); // 滚动到匹配项
                recyclerView.scrollToPosition(i); // 快速跳转
                return; // 找到后跳出
            }
        }
        Toast.makeText(getContext(), "没找到该卡片！", Toast.LENGTH_SHORT).show();
    }

}