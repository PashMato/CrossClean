package com.example.cross_clean.cross_clean.records;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecordsDao {
    @Insert
    long insert(Record record);
    @Query("SELECT * FROM Records ORDER BY score DESC LIMIT 1")
    Record getRecordWithMaxScore();

    @Query("SELECT * FROM Records ORDER BY -score")
    List <Record> getAllRecordsByScore();
    @Query("SELECT * FROM Records")
    List<Record> getAllRecords();

    @Query("SELECT * FROM Records WHERE id = :id")
    Record getRecordById(int id);

    @Query("DELETE FROM Records")
    void deleteAll();

    @Query("DELETE FROM Records WHERE id = :id")
    void delete(int id);
}
