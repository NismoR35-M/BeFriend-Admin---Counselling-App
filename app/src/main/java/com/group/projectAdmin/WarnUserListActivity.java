package com.group.projectAdmin;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group.projectAdmin.adapter.AdapterWarnsUser;
import com.group.projectAdmin.model.UserModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public class WarnUserListActivity extends AppCompatActivity {

    //User
    String id;
    private RecyclerView users_rv;
    private List<UserModel> userList;
    private AdapterWarnsUser adapterUsers;
    List<String> idList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who);

        id = getIntent().getStringExtra("id");

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        //User
        users_rv = findViewById(R.id.list);
        users_rv.setLayoutManager(new LinearLayoutManager(WarnUserListActivity.this));
        userList = new ArrayList<>();
        idList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("warn").child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    idList.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        idList.add(snapshot1.getKey());
                    }
                    showUsers();
                }else {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    users_rv.setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //EdiText
        EditText editText = findViewById(R.id.editText);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filter(editText.getText().toString());
                return true;
            }
            return false;
        });

    }

    private void filter(String query) {
        FirebaseFirestore.getInstance().collection("users").addSnapshotListener((value, error) -> {
            userList.clear();
            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                if (ds.get("name").toString().toLowerCase().contains(query.toLowerCase())){
                    for (String id : idList){
                        if (id.equals(ds.get("id").toString())){
                            userList.add(ds.toObject(UserModel.class));
                        }
                    }
                }
                adapterUsers = new AdapterWarnsUser(WarnUserListActivity.this, userList);
                users_rv.setAdapter(adapterUsers);
                if (adapterUsers.getItemCount() == 0){
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    users_rv.setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    users_rv.setVisibility(View.VISIBLE);
                    findViewById(R.id.nothing).setVisibility(View.GONE);
                }
            }
        });
    }

    private void showUsers() {
        FirebaseFirestore.getInstance().collection("users").addSnapshotListener((value, error) -> {
            userList.clear();
            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                for (String id : idList){
                    if (id.equals(ds.get("id").toString())){
                        userList.add(ds.toObject(UserModel.class));
                    }
                }
                adapterUsers = new AdapterWarnsUser(WarnUserListActivity.this, userList);
                users_rv.setAdapter(adapterUsers);
                if (adapterUsers.getItemCount() == 0){
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    users_rv.setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    users_rv.setVisibility(View.VISIBLE);
                    findViewById(R.id.nothing).setVisibility(View.GONE);
                }
            }
        });
    }

}