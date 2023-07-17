package com.anga.Elipsis_LMS.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;
import com.anga.Elipsis_LMS.Adapters.AccountUserAdapter;
import com.anga.Elipsis_LMS.AuthActivity;
import com.anga.Elipsis_LMS.Constant;
import com.anga.Elipsis_LMS.EditUserInfoActivity;
import com.anga.Elipsis_LMS.HomeActivity;
import com.anga.Elipsis_LMS.Models.User;
import com.anga.Elipsis_LMS.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment {

    private View view;
    private MaterialToolbar toolbar;
    private CircleImageView imgProfile;
    private TextView txtName, txtuserssCount,alluseravailable;
    private Button btnEditAccount;
    private RecyclerView recyclerView;
    private LinearLayout usercount;
    private ArrayList<User> arrayList;
    private SharedPreferences preferences;
    private AccountUserAdapter adapter;
    private String imgUrl = "";

    public AccountFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_account,container,false);
        init();
        return view;
    }

    private void init() {
        preferences = getContext().getSharedPreferences("user",Context.MODE_PRIVATE);
        toolbar = view.findViewById(R.id.toolbarAccount);
        usercount = view.findViewById(R.id.countUser);
        alluseravailable =view.findViewById(R.id.alluseravailable);
        ((HomeActivity)getContext()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        imgProfile = view.findViewById(R.id.imgAccountProfile);
        txtName = view.findViewById(R.id.txtAccountName);
        txtuserssCount = view.findViewById(R.id.txtAccountUserCount);
        recyclerView = view.findViewById(R.id.recyclerAccount);
        btnEditAccount = view.findViewById(R.id.btnEditAccount);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));

        if (preferences.getInt("role",2)!=1){
            usercount.setVisibility(View.GONE);
            alluseravailable.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }

        btnEditAccount.setOnClickListener(v->{
            Intent i = new Intent(((HomeActivity)getContext()), EditUserInfoActivity.class);
            i.putExtra("from", "self");
            startActivity(i);
        });
    }

    private void getAllUsersData() {
        arrayList = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET,Constant.ALL_USER,res->{

            try {
                JSONObject object = new JSONObject(res);
                if (object.getBoolean("success")){
                    JSONArray users = object.getJSONArray("users");
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject userObject = users.getJSONObject(i);

                        User user = new User();

                        user.setId(userObject.getInt("id"));
                        user.setName(userObject.getString("name"));
                        user.setEmail(userObject.getString("email"));
                        user.setRole(userObject.getInt("role"));

                        if (user.getId()==preferences.getInt("id",0)){
                            txtName.setText(preferences.getString("name","")+"\n"+preferences.getString("email",""));
                        }

                        if (user.getRole()!=1){
                            arrayList.add(user);
                        }


                    }
                    txtuserssCount.setText(arrayList.size()+"");
                    adapter = new AccountUserAdapter(getContext(),arrayList);
                    recyclerView.setAdapter(adapter);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        },error -> {
            error.printStackTrace();
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = preferences.getString("token","");
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
        inflater.inflate(R.menu.menu_account,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.item_logout: {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Do you want to logout?");
                builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout(){
        StringRequest request = new StringRequest(Request.Method.GET,Constant.LOGOUT,res->{

            try {
                JSONObject object = new JSONObject(res);
                if (object.getBoolean("success")){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                    startActivity(new Intent(((HomeActivity)getContext()), AuthActivity.class));
                    ((HomeActivity)getContext()).finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        },error -> {
            error.printStackTrace();
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = preferences.getString("token","");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+token);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {

        if (!hidden){
            getAllUsersData();
        }

        super.onHiddenChanged(hidden);
    }

    @Override
    public void onResume() {
        super.onResume();
        getAllUsersData();
    }
}
