package com.example.pia_claseordinaria;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PendingUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private List<String> userIdList;
    private FirebaseFirestore db;
    private ImageButton buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_users);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewPendingUsers);
        buttonBack = findViewById(R.id.buttonBack);

        userList = new ArrayList<>();
        userIdList = new ArrayList<>();
        adapter = new UserAdapter(userList, userIdList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        buttonBack.setOnClickListener(v -> finish());

        loadPendingUsers();
    }

    private void loadPendingUsers() {
        db.collection("usuarios")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    userIdList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        userList.add(user);
                        userIdList.add(document.getId());
                    }
                    adapter.notifyDataSetChanged();
                    if (userList.isEmpty()) {
                        Toast.makeText(this, "No hay usuarios pendientes", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
                });
    }
}
