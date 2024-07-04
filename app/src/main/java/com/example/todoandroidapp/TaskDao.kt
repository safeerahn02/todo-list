package com.example.todoandroidapp

import androidx.room.*

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTasks(tasks: List<Task>)

    @Update
    fun updateTask(task: Task)

    @Delete
    fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun getTaskById(id: Long): Task?

    @Query("DELETE FROM tasks")
    fun deleteAllTasks()
}
