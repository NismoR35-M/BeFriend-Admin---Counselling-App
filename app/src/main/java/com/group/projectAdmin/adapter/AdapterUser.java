package com.group.projectAdmin.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.group.projectAdmin.R;
import com.group.projectAdmin.model.UserModel;
import com.group.projectAdmin.notification.Data;
import com.group.projectAdmin.notification.Sender;
import com.group.projectAdmin.notification.Token;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder>{

    final Context context;
    final List<UserModel> createModels;
    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterUser(Context context, List<UserModel> createModels) {
        this.context = context;
        this.createModels = createModels;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.create_user_list, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        requestQueue = Volley.newRequestQueue(context);

        holder.setIsRecyclable(false);
        holder.name.setText(createModels.get(position).getName());
        holder.phone.setText(createModels.get(position).getPhone());
        if (!createModels.get(position).getPhoto().isEmpty()){
            Picasso.get().load(createModels.get(position).getPhoto()).into(holder.dp);
        }


        holder.itemView.setOnClickListener(view -> {
            String[] colors = {"Send warning", "Delete user", "View profile"};

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Pick a action");
            builder.setItems(colors, (dialog, which) -> {
                if (which == 1){
                    Snackbar.make(view, "Deleting...", Snackbar.LENGTH_SHORT).show();
                    //ChatList
                    FirebaseDatabase.getInstance().getReference().child("Chatlist").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                FirebaseDatabase.getInstance().getReference().child("Chatlist").child(ds.getKey()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                            }
                            FirebaseDatabase.getInstance().getReference().child("Chatlist").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    //Chat
                    FirebaseDatabase.getInstance().getReference().child("Chats").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                if (ds.child("sender").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    FirebaseDatabase.getInstance().getReference().child("Chats").child(ds.getKey()).getRef().removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    FirebaseDatabase.getInstance().getReference().child("Chats").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                if (ds.child("receiver").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    FirebaseDatabase.getInstance().getReference().child("Chats").child(ds.getKey()).getRef().removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    FirebaseDatabase.getInstance().getReference().child("calling").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                if (ds.child("from").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) || ds.child("to").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    FirebaseDatabase.getInstance().getReference().child("calling").child(ds.getKey()).getRef().removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    //Group
                    FirebaseDatabase.getInstance().getReference().child("groups").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){

                                FirebaseDatabase.getInstance().getReference().child("groups").child(ds.getKey()).child("Participants").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            snapshot.getRef().removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                FirebaseDatabase.getInstance().getReference().child("groups").child(ds.getKey()).child("Message").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            if (ds.child("sender").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                                FirebaseDatabase.getInstance().getReference("groups").child(ds.getKey()).child("Message").child(ds.getKey()).getRef().removeValue();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    //Token
                    FirebaseDatabase.getInstance().getReference().child("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                    //Users
                    FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                    //FireStore
                    FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).delete();
                    FirebaseFirestore.getInstance().collection("privacy").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).delete().addOnCompleteListener(task -> {
                        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                        params.height = 0;
                        holder.itemView.setLayoutParams(params);
                        Snackbar.make(view, "Deleted", Snackbar.LENGTH_SHORT).show();
                    });
                }
                else if (which == 0){

                    Snackbar.make(view, "Warning sent", Snackbar.LENGTH_LONG).show();
                    FirebaseDatabase.getInstance().getReference("warn").child("user").child(createModels.get(position).getId()).setValue(true);
                    notify = true;
                    if (notify){
                        sendNotification(createModels.get(position).getId(), createModels.get(position).getName(), "You have got a warning by the admin");
                    }
                    notify = false;

                }
                else if (which == 2){

                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.setPackage("com.group.project");
                    whatsappIntent.putExtra(Intent.EXTRA_SUBJECT, "user");
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, createModels.get(position).getId());
                    try {
                        context.startActivity(whatsappIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(context, "Please download Main App", Toast.LENGTH_SHORT).show();
                    }

                }

            });
            builder.show();
        });

    }

    @Override
    public int getItemCount() {
        return createModels.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        CircleImageView dp;
        TextView name,phone;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            phone = itemView.findViewById(R.id.phone);

        }

    }

    private void sendNotification(final String hisId, final String name,final String message){

        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    com.group.projectAdmin.notification.Token token = ds.getValue(Token.class);
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " " + message, "Warning", hisId, "profile", R.drawable.logo);
                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());
                    try {
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", jsonObject, response -> Timber.d("onResponse%s", response.toString()), error -> Timber.d("onResponse%s", error.toString())){
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=FIREBASE_AUTH_KEY_MESSAGING");
                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


}
