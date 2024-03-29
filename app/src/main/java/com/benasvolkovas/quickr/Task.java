package com.benasvolkovas.quickr;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Single Task has id and content
@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "content")
    public String content;
}