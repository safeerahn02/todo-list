package com.example.todoandroidapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String,
    var firebaseId: String = UUID.randomUUID().toString()
)
{
    // Required empty constructor for Firebase deserialization
    constructor() : this(0, "", "")
}
