package com.anga.Elipsis_LMS.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.anga.Elipsis_LMS.Constant;
import com.anga.Elipsis_LMS.EditUserInfoActivity;
import com.anga.Elipsis_LMS.HomeActivity;
import com.anga.Elipsis_LMS.Models.User;
import com.anga.Elipsis_LMS.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AccountUserAdapter extends RecyclerView.Adapter<AccountUserAdapter.AccountPostHolder>{

    private Context context;
    private ArrayList<User> arrayList;
    private SharedPreferences preferences;

    public AccountUserAdapter(Context context, ArrayList<User> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        preferences = context.getApplicationContext().getSharedPreferences("user",Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public AccountPostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_account_post,parent,false);
        return new AccountPostHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountPostHolder holder, int position) {
        User user = arrayList.get(position);
        holder.txtName.setText(user.getName());
        holder.txtEmail.setText(user.getEmail());

        if(preferences.getInt("role",2)==1){
            holder.btnPostOption.setVisibility(View.VISIBLE);
        } else {
            holder.btnPostOption.setVisibility(View.GONE);
        }

        holder.btnPostOption.setOnClickListener(v->{
            PopupMenu popupMenu = new PopupMenu(context,holder.btnPostOption);
            popupMenu.inflate(R.menu.menu_post_options);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    switch (item.getItemId()){
                        case R.id.item_edit: {
                            Intent i = new Intent(((HomeActivity)context), EditUserInfoActivity.class);
                            i.putExtra("userId", user.getId());
                            i.putExtra("name",user.getName());
                            i.putExtra("email", user.getEmail());
                            i.putExtra("from", "manage");
                            context.startActivity(i);
                            return true;
                        }
                        case R.id.item_delete: {
                            deleteUser(user.getId(),position);
                            return true;
                        }
                    }

                    return false;
                }
            });
            popupMenu.show();
        });

     }

    // delete user
    private void deleteUser(int UserId, int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm");
        builder.setMessage("Delete User?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringRequest request = new StringRequest(Request.Method.POST, Constant.DELETE_USER+"?id="+UserId, response -> {

                    try {
                        JSONObject object = new JSONObject(response);
                        Log.d("erorooooo",""+object);
                        if (object.getBoolean("success")){
                            Toast.makeText(context, ""+object.getString("message"), Toast.LENGTH_SHORT).show();
                            arrayList.remove(position);
                            notifyItemRemoved(position);
                            notifyDataSetChanged();
                        }else {
                            Toast.makeText(context, ""+object.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                },error -> {
                    Toast.makeText(context, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        String token = preferences.getString("token","");
                        HashMap<String,String> map = new HashMap<>();
                        map.put("Authorization","Bearer "+token);
                        return map;
                    }

//                    @Override
//                    protected Map<String, String> getParams() throws AuthFailureError {
//                        HashMap<String,String> map = new HashMap<>();
//                        map.put("id",""+UserId);
//                        return map;
//                    }
                };

                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(request);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        builder.show();
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    class AccountPostHolder extends RecyclerView.ViewHolder {

        private TextView txtName,txtEmail;
        private ImageButton btnPostOption;

        public AccountPostHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            btnPostOption = itemView.findViewById(R.id.btnPostOption);
        }
    }
}
