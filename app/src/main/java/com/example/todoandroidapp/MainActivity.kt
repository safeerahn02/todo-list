package com.example.todoandroidapp

//import TaskAdapter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
//import android.telecom.Call
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoandroidapp.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

//import retrofit2.Retrofit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var filteredtaskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private lateinit var addTodoActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var editTodoActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var database: AppDatabase
    private val titles = mutableListOf<String>()
    private val filteredTasks = mutableListOf<Task>()
    private lateinit var firebaseDatabase: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        firebaseDatabase = FirebaseDatabase.getInstance().reference.child("tasks")

        taskAdapter = TaskAdapter(tasks, this::launchEditTodoActivity, this::deleteTask)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = taskAdapter
        addTodoActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val taskName = result.data?.getStringExtra("TASK")
                    taskName?.let {
                        val task = Task(name = it)
                        insertTask(task)
                    }
                }
            }

        editTodoActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val editedTaskName = result.data?.getStringExtra("EDITED_TASK")
                    val taskPosition = result.data?.getIntExtra("TASK_POSITION", -1)
                    if (editedTaskName != null && taskPosition != null && taskPosition >= 0) {
                        val task = tasks[taskPosition]
                        updateTask(task.copy(name = editedTaskName))
                    }
                }
            }
        fetchDataFromApi()
//        binding.textView.text=titles[0]
        //loadDataFromDatabase()
        loadDataFromFirebase()
        binding.button.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            addTodoActivityLauncher.launch(intent)
        }

    }

    private fun fetchDataFromApi() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getData()
                titles.clear()
                titles.addAll(response.map { it.title })
                withContext(Dispatchers.Main) {
                    // Optionally update UI with titles if needed
                    Log.d("MainActivity", "Titles: $titles")
                }
            } catch (e: HttpException) {
                e.printStackTrace()
                Log.e("MainActivity", "HttpException: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MainActivity", "Exception: ${e.message}")
            }
        }
    }

    private fun loadDataFromDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val tasksFromDb = database.taskDao().getAllTasks()
            runOnUiThread {
                tasks.clear()
                tasks.addAll(tasksFromDb)
                taskAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun loadDataFromFirebase() {
        firebaseDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tasks.clear()
                for (snapshot in dataSnapshot.children) {
                    val task = snapshot.getValue(Task::class.java)
                    task?.let { tasks.add(it) }
                }
                taskAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to read tasks from Firebase", error.toException())
            }
        })
    }

    private fun synchronizeWithLocalDatabase(firebaseTasks: List<Task>) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Clear the local database
            database.taskDao().deleteAllTasks()
            // Insert tasks from Firebase into the local database
            database.taskDao().insertTasks(firebaseTasks)
            // Update the UI with the new data
            withContext(Dispatchers.Main) {
                tasks.clear()
                tasks.addAll(firebaseTasks)
                taskAdapter.notifyDataSetChanged()
            }
        }
    }
    private  fun insertTask(task: Task) {
        lifecycleScope.launch(Dispatchers.IO) {
            val id = database.taskDao().insertTask(task)
            task.id = id
            loadDataFromDatabase()


                firebaseDatabase.child(task.firebaseId).setValue(task)
                    .addOnSuccessListener {
                        Log.d("MainActivity", "Task added with Firebase ID: ${task.firebaseId}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("MainActivity", "Error adding task to Firebase", e)
                    }

        }
    }

    private fun updateTask(task: Task) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.taskDao().updateTask(task)
            loadDataFromDatabase()
            task.firebaseId?.let { firebaseId ->
                firebaseDatabase.child(firebaseId).setValue(task)
                    .addOnSuccessListener {
                        Log.d("MainActivity", "Task updated in Firebase with ID: $firebaseId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("MainActivity", "Error updating task in Firebase", e)
                    }
            }
        }
    }

    private fun launchEditTodoActivity(position: Int, taskName: String) {
        val intent = Intent(this, EditTodoActivity::class.java)
        intent.putExtra("TASK_NAME", taskName)
        intent.putExtra("TASK_POSITION", position)
        editTodoActivityLauncher.launch(intent)
    }

    private fun deleteTask(position: Int) {
        val taskToDelete = tasks[position]

        lifecycleScope.launch(Dispatchers.IO) {
            database.taskDao().deleteTask(taskToDelete)
            tasks.removeAt(position)
            withContext(Dispatchers.Main) {
                taskAdapter.notifyItemRemoved(position)
            }

            taskToDelete.firebaseId.let { firebaseId ->
                if(firebaseId.isEmpty()){

                }
                firebaseDatabase.child(firebaseId).removeValue()
                    .addOnSuccessListener {
                        Log.d("MainActivity", "Task deleted from Firebase with ID: $firebaseId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("MainActivity", "Error deleting task from Firebase", e)
                    }
            }
        }
    }




//    private fun deleteTask(position: Int){
//        GlobalScope.launch(Dispatchers.IO) {
//            database.taskDao().deleteTask(tasks[position])
//            tasks.removeAt(position)
//            loadDataFromDatabase()
//        }
//    }

}