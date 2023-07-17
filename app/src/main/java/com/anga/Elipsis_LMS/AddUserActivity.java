package com.anga.Elipsis_LMS;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class AddUserActivity extends AppCompatActivity {
    private TextInputLayout layoutName,layoutEmail,layoutPassword,layoutConfirm;
    private TextInputEditText txtName,txtEmail,txtPassword,txtConfirm;
    private Button btnRegister;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        init();
    }

    private void init() {
        layoutPassword = findViewById(R.id.txtLayoutPasswordSignUp);
        layoutEmail = findViewById(R.id.txtLayoutEmailSignUp);
        layoutName = findViewById(R.id.txtLayoutNameSignUp);
        layoutConfirm = findViewById(R.id.txtLayoutConfrimSignUp);
        txtPassword = findViewById(R.id.txtPasswordSignUp);
        txtConfirm = findViewById(R.id.txtConfirmSignUp);
        txtEmail = findViewById(R.id.txtEmailSignUp);
        txtName = findViewById(R.id.txtNameSignUp);
        btnRegister = findViewById(R.id.btnAddUser);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);


        btnRegister.setOnClickListener(v->{
            //validate fields first
            if (validate()){
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
                int role = sharedPreferences.getInt("role",2);
                if (role==1){
                    register();
                }else {
                    Toast.makeText(this, "Soory! Only Admin is allowed", Toast.LENGTH_SHORT).show();
                }
               
            }
        });

        txtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!txtName.getText().toString().isEmpty()){
                    layoutName.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!txtEmail.getText().toString().isEmpty()){
                    layoutEmail.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtPassword.getText().toString().length()>7){
                    layoutPassword.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtConfirm.getText().toString().equals(txtPassword.getText().toString())){
                    layoutConfirm.setErrorEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private boolean validate (){
        if (txtName.getText().toString().isEmpty()){
            layoutName.setErrorEnabled(true);
            layoutName.setError("Name is Required");
            return false;
        }
        if (txtEmail.getText().toString().isEmpty()){
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Email is Required");
            return false;
        }
        if (txtPassword.getText().toString().length()<8){
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError("Required at least 8 characters");
            return false;
        }
        if (!txtConfirm.getText().toString().equals(txtPassword.getText().toString())){
            layoutConfirm.setErrorEnabled(true);
            layoutConfirm.setError("Password does not match");
            return false;
        }


        return true;
    }


    private void register(){
        dialog.setMessage("Registering...");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Constant.ADD_USER, response -> {
            //we get response if connection success
            try {
                JSONObject object = new JSONObject(response);
                if (object.getInt("status")==200){
                    //if success
                    startActivity(new Intent((this), HomeActivity.class));
                    Toast.makeText(this, ""+object.getString("message"), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "error : "+object.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(this, "error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            dialog.dismiss();

        },error -> {
            // error if connection not success
            Toast.makeText(this, "error : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            error.printStackTrace();
            dialog.dismiss();
        }){
            // provide token in header

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
                String token = sharedPreferences.getString("token","");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+token);
                return map;
            }

            // add parameters
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("name",txtName.getText().toString().trim());
                map.put("email",txtEmail.getText().toString().trim());
                map.put("password",txtPassword.getText().toString());
                map.put("password_confirmation",txtConfirm.getText().toString());
                return map;
            }
        };

        //add this request to requestqueue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

}