package com.anga.Elipsis_LMS;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.anga.Elipsis_LMS.Models.Book;
import com.anga.Elipsis_LMS.Models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddBookActivity extends AppCompatActivity {
    private Button btnPost;
    private EditText txtDesc;
    private EditText txtTittle;
    private ProgressDialog dialog;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        init();
    }

    private void init() {
        preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        btnPost = findViewById(R.id.btnAddPost);
        txtDesc = findViewById(R.id.txtDescAddPost);
        txtTittle = findViewById(R.id.txtTittle);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);

        btnPost.setOnClickListener(v->{
            if(!txtTittle.getText().toString().isEmpty()){
                if (!txtDesc.getText().toString().isEmpty()){
                    post();
                }else {
                    Toast.makeText(this, "description is required", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void post(){
        dialog.setMessage("Posting..");
        dialog.show();

        StringRequest request = new StringRequest(Request.Method.POST,Constant.ADD_BOOK, response -> {

            try {
                JSONObject object = new JSONObject(response);

                if (object.getInt("status")==200){
                    JSONObject bookObject = object.getJSONObject("data");
                    JSONObject userObject = bookObject.getJSONObject("user");

                    User user = new User();
                    user.setId(userObject.getInt("id"));
                    user.setName(userObject.getString("name"));

                    Book book = new Book();
                    book.setUser(user);
                    book.setId(bookObject.getInt("id"));
                    book.setDesc(bookObject.getString("title"));
                    book.setComments(0);
                    book.setLikes(0);
                    book.setDate(bookObject.getString("created_at"));

                    startActivity(new Intent((this), HomeActivity.class));
                    Toast.makeText(this, ""+object.getString("message"), Toast.LENGTH_SHORT).show();
                    finish();


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog.dismiss();

        },error -> {
                error.printStackTrace();
                dialog.dismiss();
        }){

            // add token to header


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = preferences.getString("token","");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+token);
                return map;
            }

            // add params

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("title",txtTittle.getText().toString().trim());
                map.put("content",txtDesc.getText().toString().trim());
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(AddBookActivity.this);
        queue.add(request);

    }

    public void cancelPost(View view) {
        super.onBackPressed();
    }

}
