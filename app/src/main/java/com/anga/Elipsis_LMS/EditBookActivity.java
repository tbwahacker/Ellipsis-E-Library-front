package com.anga.Elipsis_LMS;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
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
import com.anga.Elipsis_LMS.Fragments.HomeFragment;
import com.anga.Elipsis_LMS.Models.Book;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditBookActivity extends AppCompatActivity {

    private int position =0, id= 0;
    private EditText txtDesc,txtTitle;
    private Button btnSave;
    private ProgressDialog dialog;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        init();
    }

    private void init() {
        sharedPreferences = getApplication().getSharedPreferences("user", Context.MODE_PRIVATE);
        txtDesc = findViewById(R.id.txtDescEditPost);
        txtTitle = findViewById(R.id.txtTittle);
        btnSave = findViewById(R.id.btnEditPost);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        position = getIntent().getIntExtra("position",0);
        id = getIntent().getIntExtra("postId",0);
        txtDesc.setText(getIntent().getStringExtra("text"));
        txtTitle.setText(getIntent().getStringExtra("title"));

        btnSave.setOnClickListener(v->{
            if (!txtTitle.getText().toString().isEmpty()){
                if (!txtDesc.getText().toString().isEmpty()){
                    savePost();
                }
            }

        });
    }

    private void savePost() {
        dialog.setMessage("Saving");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.PUT,Constant.UPDATE_POST,response -> {

            try {
                JSONObject object = new JSONObject(response);
                if (object.getInt("status")==200){
                    // update the book in recycler view
                    Book book = HomeFragment.arrayList.get(position);
                    book.setDesc(txtDesc.getText().toString());
                    book.setTitle(txtTitle.getText().toString());
                    HomeFragment.arrayList.set(position, book);
                    HomeFragment.recyclerView.getAdapter().notifyItemChanged(position);
                    HomeFragment.recyclerView.getAdapter().notifyDataSetChanged();
//                    startActivity(new Intent(EditBookActivity.this,HomeActivity.class));
                    Toast.makeText(this, "Book Edited", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        },error -> {
            error.printStackTrace();
        }){

            //add token to header


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = sharedPreferences.getString("token","");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+token);
                return map;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("id",id+"");
                map.put("content",txtDesc.getText().toString());
                map.put("title",txtTitle.getText().toString());
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(EditBookActivity.this);
        queue.add(request);
    }

    public void cancelEdit(View view){
        super.onBackPressed();
    }
}






















