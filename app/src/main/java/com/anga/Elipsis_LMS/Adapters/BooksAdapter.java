package com.anga.Elipsis_LMS.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.anga.Elipsis_LMS.CommentActivity;
import com.anga.Elipsis_LMS.Constant;
import com.anga.Elipsis_LMS.EditBookActivity;
import com.anga.Elipsis_LMS.HomeActivity;
import com.anga.Elipsis_LMS.Models.Book;
import com.anga.Elipsis_LMS.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.PostsHolder> {


    private Context context;
    private ArrayList<Book> list;
    private ArrayList<Book> listAll;
    private SharedPreferences preferences;

    public BooksAdapter(Context context, ArrayList<Book> list) {
        this.context = context;
        this.list = list;
        this.listAll = new ArrayList<>(list);
        preferences = context.getApplicationContext().getSharedPreferences("user",Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public PostsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post,parent,false);
        return new PostsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostsHolder holder, int position) {
        Book book = list.get(position);
        holder.txtTitle.setText(book.getTitle());
        holder.txtComments.setText("View all "+ book.getComments()+" comments");
        holder.txtLikes.setText(book.getLikes()+" Likes");
        holder.txtDate.setText(book.getDate());
        holder.txtDesc.setText(book.getDesc());

        holder.btnLike.setImageResource(
                book.isSelfLike()?R.drawable.ic_favorite_red:R.drawable.ic_favorite_outline
        );
        // like click
        holder.btnLike.setOnClickListener(v->{
            holder.btnLike.setImageResource(
                    book.isSelfLike()?R.drawable.ic_favorite_outline:R.drawable.ic_favorite_red
            );

            StringRequest request = new StringRequest(Request.Method.POST,Constant.LIKE_POST,response -> {

                Book mBook = list.get(position);

                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getBoolean("success")){
                        mBook.setSelfLike(!book.isSelfLike());
                        mBook.setLikes(mBook.isSelfLike()? book.getLikes()+1: book.getLikes()-1);
                        list.set(position, mBook);
                        notifyItemChanged(position);
                        notifyDataSetChanged();
                    }
                    else {
                        holder.btnLike.setImageResource(
                                book.isSelfLike()?R.drawable.ic_favorite_red:R.drawable.ic_favorite_outline
                        );
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            },err->{
                err.printStackTrace();
            }){
                // add token

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
                    map.put("id", book.getId()+"");
                    return map;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(request);

        });

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
                            Intent i = new Intent(((HomeActivity)context), EditBookActivity.class);
                            i.putExtra("postId", book.getId());
                            i.putExtra("position",position);
                            i.putExtra("text", book.getDesc());
                            i.putExtra("title", book.getTitle());
                            context.startActivity(i);
                            return true;
                        }
                        case R.id.item_delete: {
                            deleteBook(book.getId(),position);
                            return true;
                        }
                    }

                    return false;
                }
            });
            popupMenu.show();
        });

        holder.txtComments.setOnClickListener(v->{
            Intent i = new Intent(((HomeActivity)context), CommentActivity.class);
            i.putExtra("postId", book.getId());
            i.putExtra("postPosition",position);
            context.startActivity(i);
        });

        holder.btnComment.setOnClickListener(v->{
            Intent i = new Intent(((HomeActivity)context),CommentActivity.class);
            i.putExtra("postId", book.getId());
            i.putExtra("postPosition",position);
            context.startActivity(i);
        });

    }

    // delete post
    private void deleteBook(int postId, int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm");
        builder.setMessage("Delete Book?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringRequest request = new StringRequest(Request.Method.DELETE,Constant.DELETE_POST+"?id="+postId,response -> {

                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.getInt("status")==200){
                            list.remove(position);
                            notifyItemRemoved(position);
                            notifyDataSetChanged();
                            listAll.clear();
                            listAll.addAll(list);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                },error -> {

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
//                        map.put("id",postId+"");
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
        return list.size();
    }


    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            ArrayList<Book> filteredList = new ArrayList<>();
            if (constraint.toString().isEmpty()){
                filteredList.addAll(listAll);
            } else {
                for (Book book : listAll){
                    if(book.getDesc().toLowerCase().contains(constraint.toString().toLowerCase())
                    || book.getUser().getName().toLowerCase().contains(constraint.toString().toLowerCase())){
                        filteredList.add(book);
                    }
                }

            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return  results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((Collection<? extends Book>) results.values);
            notifyDataSetChanged();
        }
    };

    public Filter getFilter() {
        return filter;
    }

    class PostsHolder extends RecyclerView.ViewHolder{

        private TextView txtTitle,txtDate,txtDesc,txtLikes,txtComments;
        private CircleImageView imgProfile;
        private ImageView imgPost;
        private ImageButton btnPostOption,btnLike,btnComment;

        public PostsHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtPostTitle);
            txtDate = itemView.findViewById(R.id.txtPostDate);
            txtDesc = itemView.findViewById(R.id.txtPostDesc);
            txtLikes = itemView.findViewById(R.id.txtPostLikes);
            txtComments = itemView.findViewById(R.id.txtPostComments);
            imgProfile = itemView.findViewById(R.id.imgPostProfile);
            btnPostOption = itemView.findViewById(R.id.btnPostOption);
            btnLike = itemView.findViewById(R.id.btnPostLike);
            btnComment = itemView.findViewById(R.id.btnPostComment);
            btnPostOption.setVisibility(View.GONE);
        }
    }
}