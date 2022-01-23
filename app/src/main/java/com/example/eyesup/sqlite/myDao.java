package com.example.eyesup.sqlite;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface myDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addNote(Notes item);

    @Query("select * from notes")
    public List<Notes> getAllNotes();

    @Query("select * from notes where user_id = :id1")
    public List<Notes> getUserNotes(int id1);

    @Query("select * from notes where id = :id1")
    public Notes getNote(int id1);

    @Delete
    public void deleteNote(Notes item);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public void updateNote(Notes item);

    @Delete
    public void deleteAllNotes(List<Notes> items);
}
