package com.example.todoandroidapp

//import TaskAdapter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
//import android.telecom.Call
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoandroidapp.databinding.ActivityMainBinding
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
    private val tasks = mutableListOf<Task>()
    private lateinit var addTodoActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var editTodoActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var database: AppDatabase
    private val titles = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        taskAdapter = TaskAdapter(tasks, this::launchEditTodoActivity)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = taskAdapter

        addTodoActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val taskName = result.data?.getStringExtra("TASK")
                taskName?.let {
                    val task = Task(name = it)
                    insertTask(task)
                }
            }
        }

        editTodoActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
        loadDataFromDatabase()

        binding.button.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            addTodoActivityLauncher.launch(intent)
        }
    }
    private fun fetchDataFromApi() {
        GlobalScope.launch(Dispatchers.IO) {
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
        GlobalScope.launch(Dispatchers.IO) {
            val tasksFromDb = database.taskDao().getAllTasks()
            runOnUiThread {
                tasks.clear()
                tasks.addAll(tasksFromDb)
                taskAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun insertTask(task: Task) {
        GlobalScope.launch(Dispatchers.IO) {
            database.taskDao().insertTask(task)
            loadDataFromDatabase()
        }
    }

    private fun updateTask(task: Task) {
        GlobalScope.launch(Dispatchers.IO) {
            database.taskDao().updateTask(task)
            loadDataFromDatabase()
        }
    }

    private fun launchEditTodoActivity(position: Int, taskName: String) {
        val intent = Intent(this, EditTodoActivity::class.java)
        intent.putExtra("TASK_NAME", taskName)
        intent.putExtra("TASK_POSITION", position)
        editTodoActivityLauncher.launch(intent)
    }
}