package com.example.pia_claseordinaria;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private List<String> userIdList;

    public UserAdapter(List<User> userList, List<String> userIdList) {
        this.userList = userList;
        this.userIdList = userIdList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        String userId = userIdList.get(position);

        holder.textViewName.setText(user.fullName);
        holder.textViewEmail.setText(user.email);
        holder.textViewAddress.setText(user.address);

        holder.buttonActivate.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("usuarios").document(userId)
                    .update("status", "active")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(v.getContext(), "Usuario activado con éxito", Toast.LENGTH_SHORT).show();
                        userList.remove(position);
                        userIdList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, userList.size());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(v.getContext(), "Error al activar usuario", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewEmail, textViewAddress;
        Button buttonActivate;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewUserName);
            textViewEmail = itemView.findViewById(R.id.textViewUserEmail);
            textViewAddress = itemView.findViewById(R.id.textViewUserAddress);
            buttonActivate = itemView.findViewById(R.id.buttonActivateUser);
        }
    }
}
