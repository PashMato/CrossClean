package com.example.cross_clean.cross_clean.records;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "Records")
public class Record {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String owner;
    public int score;
    public String date;

    public Record() {
        this("", 0, new Date());
    }

    public Record(String _owner, int _score, Date date) {
        owner = _owner;
        score = _score;

        this.date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
    }
}
