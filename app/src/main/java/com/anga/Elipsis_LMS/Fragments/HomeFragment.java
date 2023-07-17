package com.anga.Elipsis_LMS.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;
import com.anga.Elipsis_LMS.Adapters.BooksAdapter;
import com.anga.Elipsis_LMS.Constant;
import com.anga.Elipsis_LMS.HomeActivity;
import com.anga.Elipsis_LMS.Models.Book;
import com.anga.Elipsis_LMS.Models.User;
import com.anga.Elipsis_LMS.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    private View view;
    public static RecyclerView recyclerView;
    public static ArrayList<Book> arrayList;
    private SwipeRefreshLayout refreshLayout;
    private BooksAdapter booksAdapter;
    private MaterialToolbar toolbar;
    private ProgressBar loadingPB;
    private NestedScrollView nestedSV;
    private SharedPreferences sharedPreferences;

    public HomeFragment(){}

    // creating a variable for our page and limit as 2
    // as our api is having highest limit as 2 so
    // we are setting a limit = 2
    int page = 1, limit = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_home,container,false);
        init();
        return view;
    }

    private void init(){
        sharedPreferences = getContext().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        recyclerView = view.findViewById(R.id.recyclerHome);
        loadingPB = view.findViewById(R.id.idPBLoading);
        nestedSV =  view.findViewById(R.id.idNestedSV);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayout = view.findViewById(R.id.swipeHome);
        toolbar = view.findViewById(R.id.toolbarHome);
        ((HomeActivity)getContext()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        // adding on scroll change listener method for our nested scroll view.
        nestedSV.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // on scroll change we are checking when users scroll as bottom.
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    // in this method we are incrementing page number,
                    // making progress bar visible and calling get data method.
                    page++;
                    loadingPB.setVisibility(View.VISIBLE);
                    getPosts(page,limit);
                }
            }
        });


        getPosts(page,limit);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPosts(page, limit);
            }
        });
    }

    private void getPosts(int page, int limit) {

        if (page > limit) {
            // checking if the page number is greater than limit.
            // displaying toast message in this case when page>limit.
            Toast.makeText(getContext(), "That's all the books..", Toast.LENGTH_SHORT).show();

            // hiding our progress bar.
            loadingPB.setVisibility(View.GONE);
            return;
        }

        arrayList = new ArrayList<>();
        refreshLayout.setRefreshing(true);

        StringRequest request = new StringRequest(Request.Method.GET, Constant.BOOKS+"?page=" + page, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if (object.getInt("status")==200){
                    JSONArray array = new JSONArray(object.getString("data"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject bookObject = array.getJSONObject(i);
                        JSONObject userObject = bookObject.getJSONObject("user");

                        User user = new User();
                        user.setId(userObject.getInt("id"));
                        user.setName(userObject.getString("name"));
                        user.setRole(userObject.getInt("role"));

                        Book book = new Book();
                        book.setId(bookObject.getInt("id"));
                        book.setUser(user);
                        book.setLikes(bookObject.getInt("likesCount"));
                        book.setComments(bookObject.getInt("commentsCount"));
                        book.setDate(bookObject.getString("created_at"));
                        book.setDesc(bookObject.getString("content"));
                        book.setTitle(bookObject.getString("title"));
                        book.setSelfLike(bookObject.getBoolean("selfLike"));

                        arrayList.add(book);
                    }

                    booksAdapter = new BooksAdapter(getContext(),arrayList);
                    recyclerView.setAdapter(booksAdapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            refreshLayout.setRefreshing(false);

        },error -> {
            error.printStackTrace();
            refreshLayout.setRefreshing(false);
        }){

            // provide token in header

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = sharedPreferences.getString("token","");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+token);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search,menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView)item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (booksAdapter != null){
                    booksAdapter.getFilter().filter(newText);
                }

                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
}
