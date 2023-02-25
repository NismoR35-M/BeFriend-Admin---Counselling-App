package com.group.projectAdmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.group.projectAdmin.auth.GenerateOTPActivity;
import com.group.projectAdmin.model.UserModel;
import com.group.projectAdmin.notification.Data;
import com.group.projectAdmin.notification.Sender;
import com.group.projectAdmin.notification.Token;
import com.group.projectAdmin.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(MainActivity.this);

        findViewById(R.id.logout).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, GenerateOTPActivity.class));
            finish();
        });

        findViewById(R.id.users).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, UserListActivity.class)));
        findViewById(R.id.warnUser).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, WarnUserListActivity.class)));
        findViewById(R.id.online).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, UserOnlineListActivity.class)));
        findViewById(R.id.groups).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, GroupListActivity.class)));
        findViewById(R.id.reportedUser).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ReportUserListActivity.class)));

        TextView userNo = findViewById(R.id.userNo);
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userNo.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //announcement
        findViewById(R.id.announcement).setOnClickListener(v -> findViewById(R.id.extra).setVisibility(View.VISIBLE));

        findViewById(R.id.imageView4).setOnClickListener(v -> findViewById(R.id.extra).setVisibility(View.GONE));

        EditText email = findViewById(R.id.email);
        findViewById(R.id.login).setOnClickListener(v -> {
            if (email.getText().toString().isEmpty()){
                Snackbar.make(v, "Enter a message", Snackbar.LENGTH_SHORT).show();
            }else {

                FirebaseFirestore.getInstance().collection("users").addSnapshotListener((value, error) -> {
                    notify = true;
                    for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                        UserModel user = ds.toObject(UserModel.class);
                        assert user != null;
                        if (notify) {
                            sendNotification(user.getId(), user.getName(), email.getText().toString());
                        }
                    }
                    notify = false;
                    Toast.makeText(MainActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.extra).setVisibility(View.GONE);


                });


            }
        });

        TextView groupNo = findViewById(R.id.groupsNo);
        FirebaseDatabase.getInstance().getReference().child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupNo.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        TextView partyNo = findViewById(R.id.partyNo);
        FirebaseDatabase.getInstance().getReference().child("Party").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                partyNo.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        TextView meetNO = findViewById(R.id.meetNO);
        FirebaseDatabase.getInstance().getReference().child("Chats").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                meetNO.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        TextView onlineNo = findViewById(R.id.onlineNo);
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot ds : snapshot.getChildren()){
                    if (ds.child("last").getValue().toString().equals("online")){
                        i++;
                    }
                }
                onlineNo.setText(""+i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendNotification(final String hisId, final String name,final String message){

        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " " + message, "Announcement", hisId, "profile", R.drawable.logo);
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
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}