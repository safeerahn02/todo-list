package com.example.todoandroidapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoandroidapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private lateinit var addTodoActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var editTodoActivityLauncher: ActivityResultLauncher<Intent>
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskAdapter = TaskAdapter(tasks, this::launchEditTodoActivity, this::deleteTask)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = taskAdapter

        addTodoActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val taskName = result.data?.getStringExtra("TASK")
                    taskName?.let {
                        val task = Task(name = it)
                        viewModel.insertTask(task)
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
                        viewModel.updateTask(task.copy(name = editedTaskName))
                    }
                }
            }

        viewModel.tasks.observe(this, Observer { tasks ->
            this.tasks.clear()
            this.tasks.addAll(tasks)
            taskAdapter.notifyDataSetChanged()
        })

        viewModel.titles.observe(this, Observer { titles ->
            Log.d("MainActivity", "Titles: $titles")
        })

        binding.button.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            addTodoActivityLauncher.launch(intent)
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
        viewModel.deleteTask(taskToDelete)
    }
}
