package com.group.projectAdmin;

import android.annotation.SuppressLint;
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
import com.group.projectAdmin.adapter.AdapterUser;
import com.group.projectAdmin.model.UserModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public class UserOnlineListActivity extends AppCompatActivity {

    //User
    String id;
    private RecyclerView users_rv;
    private List<UserModel> userList;
    private AdapterUser adapterUsers;
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
        users_rv.setLayoutManager(new LinearLayoutManager(UserOnlineListActivity.this));
        userList = new ArrayList<>();
        idList = new ArrayList<>();
        getOnline();

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

    private void getOnline() {
        FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    if (ds.child("last").getValue().toString().equals("online")) {
                        idList.add(ds.getKey());
                    }
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void filter(String query) {
        FirebaseFirestore.getInstance().collection("users").addSnapshotListener((value, error) -> {
            userList.clear();
            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                if (ds.get("name").toString().toLowerCase().contains(query.toLowerCase())){
                    for (String id : idList){
                        if (ds.get("id").toString().equals(id)){
                            userList.add(ds.toObject(UserModel.class));
                        }
                    }
                }
                adapterUsers = new AdapterUser(UserOnlineListActivity.this, userList);
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
                    if (ds.get("id").toString().equals(id)){
                        userList.add(ds.toObject(UserModel.class));
                    }
                }
                adapterUsers = new AdapterUser(UserOnlineListActivity.this, userList);
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