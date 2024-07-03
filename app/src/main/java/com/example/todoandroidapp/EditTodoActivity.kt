package com.example.todoandroidapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.todoandroidapp.databinding.ActivityEditTodoBinding

class EditTodoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditTodoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val taskName = intent.getStringExtra("TASK_NAME")
        val taskPosition = intent.getIntExtra("TASK_POSITION", -1)

        binding.taskInput.setText(taskName)

        binding.saveButton.setOnClickListener {
            val editedTask = binding.taskInput.text.toString()
            val resultIntent = Intent()
            resultIntent.putExtra("EDITED_TASK", editedTask)
            resultIntent.putExtra("TASK_POSITION", taskPosition)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}