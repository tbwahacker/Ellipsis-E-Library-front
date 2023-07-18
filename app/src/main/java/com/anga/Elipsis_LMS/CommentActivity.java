package com.anga.Elipsis_LMS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.anga.Elipsis_LMS.Adapters.CommentsAdapter;
import com.anga.Elipsis_LMS.Fragments.HomeFragment;
import com.anga.Elipsis_LMS.Models.Book;
import com.anga.Elipsis_LMS.Models.Comment;
import com.anga.Elipsis_LMS.Models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<Comment> list;
    private CommentsAdapter adapter;
    private int bookId = 0;
    public  static  int postPosition = 0;
    private SharedPreferences preferences;
    private EditText txtAddComment;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        init();
    }

    private void init() {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        postPosition = getIntent().getIntExtra("postPosition",-1);
        preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        recyclerView = findViewById(R.id.recyclerComments);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        txtAddComment = findViewById(R.id.txtAddComment);
        bookId = getIntent().getIntExtra("postId",0);
        getComments();
    }

    private void getComments() {
        list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST,Constant.COMMENTS,res->{

            try {
                JSONObject object = new JSONObject(res);
                if (object.getBoolean("success")){
                    JSONArray comments = new JSONArray(object.getString("comments"));
                    for (int i = 0; i < comments.length(); i++) {
                        JSONObject comment = comments.getJSONObject(i);
                        JSONObject user = comment.getJSONObject("user");

                        User mUser = new User();
                        mUser.setId(user.getInt("id"));
                        mUser.setName(user.getString("name"));

                        Comment mComment = new Comment();
                        mComment.setId(comment.getInt("id"));
                        mComment.setUser(mUser);
                        mComment.setDate(comment.getString("created_at"));
                        mComment.setComment(comment.getString("comment"));
                        list.add(mComment);
                    }
                }

                adapter = new CommentsAdapter(this,list);
                recyclerView.setAdapter(adapter);

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

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("id", bookId +"");
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(CommentActivity.this);
        queue.add(request);
    }

    public void goBack(View view) {
        super.onBackPressed();
    }

    public void addComment(View view) {
        String commentText = txtAddComment.getText().toString();
        dialog.setMessage("Adding comment");
        dialog.show();
        if (commentText.length()>0){
            StringRequest request = new StringRequest(Request.Method.POST,Constant.CREATE_COMMENT,res->{

                try {
                    JSONObject object = new JSONObject(res);
                    if (object.getBoolean("success")){
                        JSONObject comment = object.getJSONObject("comment");
                        JSONObject user = comment.getJSONObject("user");

                        Comment c = new Comment();
                        User u = new User();
                        u.setId(user.getInt("id"));
                        u.setName(user.getString("name"));
                        c.setUser(u);
                        c.setId(comment.getInt("id"));
                        c.setDate(comment.getString("created_at"));
                        c.setComment(comment.getString("comment"));

                        Book book = HomeFragment.arrayList.get(postPosition);
                        book.setComments(book.getComments()+1);
                        HomeFragment.arrayList.set(postPosition, book);
                        HomeFragment.recyclerView.getAdapter().notifyDataSetChanged();

                        list.add(c);
                        recyclerView.getAdapter().notifyDataSetChanged();
                        txtAddComment.setText("");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();

            },err->{
                err.printStackTrace();
                dialog.dismiss();
            }){
                //add token to header

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    String token = preferences.getString("token","");
                    HashMap<String,String> map = new HashMap<>();
                    map.put("Authorization","Bearer "+token);
                    return map;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> map = new HashMap<>();
                    map.put("id", bookId +"");
                    map.put("comment",commentText);
                    return map;
                }


            };
            RequestQueue queue = Volley.newRequestQueue(CommentActivity.this);
            queue.add(request);
        }
    }
}
