package com.example.pia_claseordinaria;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AccessHistoryAdapter extends RecyclerView.Adapter<AccessHistoryAdapter.ViewHolder> {

    private List<AccessRecord> recordList;

    public AccessHistoryAdapter(List<AccessRecord> recordList) {
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_access_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AccessRecord record = recordList.get(position);
        holder.textDateTime.setText(record.getDateTime());
        holder.textAccessKey.setText("Clave: " + record.getAccessKey());
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDateTime, textAccessKey;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            textAccessKey = itemView.findViewById(R.id.textAccessKey);
        }
    }
}
