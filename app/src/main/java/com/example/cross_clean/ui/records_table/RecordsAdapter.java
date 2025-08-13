package com.example.cross_clean.ui.records_table;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cross_clean.R;
import com.example.cross_clean.cross_clean.records.Record;

import java.util.List;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ItemViewHolder> {
    public List<Record> records;
    private Context context;

    public RecordsAdapter(List<Record> records, Context c) {
        this.records = records;
        context = c;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_frame, parent,false);
        return new ItemViewHolder(view);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        Record item = records.get(position);
        holder.id = item.id;
        holder.scoreTV.setText(context.getString(R.string.score) + " " + item.score);
        holder.ownerTV.setText(item.owner);
        holder.dateTV.setText(context.getString(R.string.date) + " " + item.date);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public Record getRecordsAt(int position) {
        return records.get(position);
    }


    class ItemViewHolder extends RecyclerView.ViewHolder {

        int id;
        TextView scoreTV;
        TextView ownerTV;
        TextView dateTV;
        CardView cardView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ownerTV = itemView.findViewById(R.id.owner_text);
            scoreTV = itemView.findViewById(R.id.score_text);
            dateTV = itemView.findViewById(R.id.date_text);
            cardView = itemView.findViewById(R.id.record_card);
        }
    }
}
