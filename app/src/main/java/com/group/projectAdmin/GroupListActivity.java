package com.group.projectAdmin;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group.projectAdmin.adapter.AdapterGroupsList;
import com.group.projectAdmin.model.ModelGroups;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class GroupListActivity extends AppCompatActivity {

    //User
    @SuppressWarnings("unused")
    String id;
    private RecyclerView users_rv;
    private List<ModelGroups> modelGroupsList;
    private AdapterGroupsList getAdapterGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who);

        id = getIntent().getStringExtra("id");

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        //User
        users_rv = findViewById(R.id.list);
        users_rv.setLayoutManager(new LinearLayoutManager(GroupListActivity.this));
        modelGroupsList = new ArrayList<>();
        showUsers();

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
        FirebaseDatabase.getInstance().getReference("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelGroupsList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if (ds.child("Participants").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        ModelGroups modelChatListGroups = ds.child("Participants").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(ModelGroups.class);
                        if (modelChatListGroups.getRole().equals(query)){
                            modelGroupsList.add(modelChatListGroups);
                        }
                    }
                    getAdapterGroups = new AdapterGroupsList(GroupListActivity.this, modelGroupsList);
                    users_rv.setAdapter(getAdapterGroups);

                    if (getAdapterGroups.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        users_rv.setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        users_rv.setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    }
                }

                if (!dataSnapshot.exists()){
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    users_rv.setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showUsers() {
        FirebaseDatabase.getInstance().getReference("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelGroupsList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if (ds.child("Participants").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        ModelGroups modelChatListGroups = ds.child("Participants").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(ModelGroups.class);
                        modelGroupsList.add(modelChatListGroups);
                    }
                    getAdapterGroups = new AdapterGroupsList(GroupListActivity.this, modelGroupsList);
                    users_rv.setAdapter(getAdapterGroups);

                    if (getAdapterGroups.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        users_rv.setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        users_rv.setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    }
                }

                if (!dataSnapshot.exists()){
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    users_rv.setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}