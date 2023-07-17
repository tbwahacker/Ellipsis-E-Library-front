package com.anga.Elipsis_LMS;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditUserInfoActivity extends AppCompatActivity {

    private TextInputLayout layoutName, layoutEmail;
    private TextInputEditText txtName, txtEmail;
    private Button btnSave;
    private CircleImageView circleImageView;
    private Bitmap bitmap = null;
    private String userId;
    private SharedPreferences userPref;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);
        init();
    }

    private void init() {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        layoutEmail = findViewById(R.id.txtEditLayoutLastnameameUserInfo);
        layoutName = findViewById(R.id.txtEditLayoutNameUserInfo);
        txtName = findViewById(R.id.txtEditNameUserInfo);
        txtEmail = findViewById(R.id.txtEditEmailUserInfo);
        btnSave = findViewById(R.id.btnEditSave);
        circleImageView = findViewById(R.id.imgEditUserInfo);

        if (getIntent() != null && getIntent().getStringExtra("from").equals("manage")){
            txtName.setText(getIntent().getStringExtra("name"));
            txtEmail.setText(getIntent().getStringExtra("email"));
            userId = ""+getIntent().getIntExtra("userId",0);
        }else {
            txtName.setText(userPref.getString("name",""));
            txtEmail.setText(userPref.getString("email",""));
            userId = ""+userPref.getInt("id",0);
        }

        btnSave.setOnClickListener(v->{
            if (validate()){
                updateProfile();
            }
        });
    }


    private void updateProfile(){
        dialog.setMessage("Updating");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST,Constant.UPDATE_USER,res->{

            try {
                JSONObject object = new JSONObject(res);
                if (object.getBoolean("success")){

                     if (! getIntent().getStringExtra("from").equals("manage")){
                         SharedPreferences.Editor editor = userPref.edit();
                         editor.putString("name",txtName.getText().toString().trim());
                         editor.putString("email", txtEmail.getText().toString().trim());
                         editor.apply();
                     }


                    Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditUserInfoActivity.this,HomeActivity.class));
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        },err->{
            err.printStackTrace();
            dialog.dismiss();
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = userPref.getString("token","");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+token);
                return map;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("name",txtName.getText().toString().trim());
                map.put("email", txtEmail.getText().toString().trim());
                map.put("id", userId);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(EditUserInfoActivity.this);
        queue.add(request);
    }

    private boolean validate(){
        if (txtName.getText().toString().isEmpty()){
            layoutName.setErrorEnabled(true);
            layoutName.setError("Name is Required");
            return false;
        }
        if (txtEmail.getText().toString().isEmpty()){
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Email is required");
            return false;
        }

        return true;
    }

}
