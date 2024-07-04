package com.example.todoandroidapp

import androidx.lifecycle.LiveData
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.CountDownLatch

class TaskRepository(private val taskDao: TaskDao, private val firebaseDatabase: DatabaseReference) {

    suspend fun fetchDataFromApi(): List<String> {
        return try {
            val response = RetrofitInstance.api.getData()
            response.map { it.title }
        } catch (e: HttpException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun loadDataFromDatabase(): List<Task> {
        return taskDao.getAllTasks()
    }

    suspend fun loadDataFromFirebase(): List<Task> {
        return withContext(Dispatchers.IO) {
            val taskList = mutableListOf<Task>()
            val latch = CountDownLatch(1)
            firebaseDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.mapNotNullTo(taskList) { it.getValue(Task::class.java) }
                    latch.countDown()
                }

                override fun onCancelled(error: DatabaseError) {
                    latch.countDown()
                }
            })
            latch.await()
            taskList
        }
    }

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
        firebaseDatabase.child(task.firebaseId).setValue(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
        firebaseDatabase.child(task.firebaseId).setValue(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
        firebaseDatabase.child(task.firebaseId).removeValue()
    }
}
