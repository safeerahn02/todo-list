package com.example.todoandroidapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.todoandroidapp.databinding.ActivityAddtodoBinding

class AddTodoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddtodoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddtodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.saveButton.setOnClickListener {
            val task = binding.taskInput.text.toString()
            if (task.isNotEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra("TASK", task)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                binding.taskInput.error = "Task cannot be empty"
            }
        }
    }
}
