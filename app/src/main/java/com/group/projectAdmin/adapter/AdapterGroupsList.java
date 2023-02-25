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
import com.group.projectAdmin.model.ModelGroups;
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
public class AdapterGroupsList extends RecyclerView.Adapter<AdapterGroupsList.MyHolder>{

    final Context context;
    final List<ModelGroups> userList;
    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterGroupsList(Context context, List<ModelGroups> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.chat_list_group, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        requestQueue = Volley.newRequestQueue(context);

        ModelGroups modelChatListGroups = userList.get(position);

        FirebaseFirestore.getInstance().collection("groups").document(modelChatListGroups.getTimestamp()).addSnapshotListener((value, error) -> {

            holder.name.setText(value.get("name").toString());

            if (!value.get("photo").toString().isEmpty()){
                Picasso.get().load(value.get("photo").toString()).into(holder.dp);
            }

        });

        holder.itemView.setOnClickListener(view -> {
            String[] colors = {"Send warning", "Delete group", "View profile"};

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Pick a action");
            builder.setItems(colors, (dialog, which) -> {
                if (which == 1){
                    Snackbar.make(view, "Deleting...", Snackbar.LENGTH_SHORT).show();

                    FirebaseDatabase.getInstance().getReference().child("Groups").child(modelChatListGroups.getId()).getRef().removeValue();
                    FirebaseFirestore.getInstance().collection("groups").document(modelChatListGroups.getId()).delete();
                    ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                    params.height = 0;
                    holder.itemView.setLayoutParams(params);
                    Snackbar.make(view, "Deleted", Snackbar.LENGTH_SHORT).show();

                }
                else if (which == 0){

                    Snackbar.make(view, "Warning sent", Snackbar.LENGTH_LONG).show();
                    FirebaseDatabase.getInstance().getReference("warn").child("group").child(modelChatListGroups.getId()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Groups").child(modelChatListGroups.getId()).child("Participants").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                notify = true;
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    if (ds.child("role").getValue().toString().equals("creator") || ds.child("role").getValue().toString().equals("admin")){
                                        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
                                            if (notify){
                                                sendNotification(ds.child("id").getValue().toString(), value.get("name").toString(), "Your group got a warning by the admin");
                                            }
                                            notify = false;
                                        });
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
                else if (which == 2){

                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.setPackage("com.group.project");
                    whatsappIntent.putExtra(Intent.EXTRA_SUBJECT, "group");
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, modelChatListGroups.getTimestamp());
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
        return userList.size();
    }

    @SuppressWarnings("unused")
    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final TextView name;
        final TextView username;
        final TextView time;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            time = itemView.findViewById(R.id.time);
            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.phone);
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
                                headers.put("Authorization", "key= AAAAMgRWZgs:APA91bHfpwh9jSIw0pqiFX9IPXhVL_woD5U4PsZAedUpOVgrpqV7IRvamJr2xtjdwcwxVdrmOigBP4SxRr4gk7u6cz83zSutL0Gypp8Olpfn2YjiSpISrM664xjSU3eS6qp7l12Ksylf");
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
