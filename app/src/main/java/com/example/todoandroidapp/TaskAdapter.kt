package com.example.todoandroidapp

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todoandroidapp.databinding.ItemTaskBinding


class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val editTaskCallback: (Int, String) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task, position: Int) {
            binding.taskTitle.text = task.name
            binding.button6.setOnClickListener {
                deleteTask(position)
            }
            binding.button5.setOnClickListener {
                editTaskCallback(position, task.name)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position], position)
    }

    override fun getItemCount(): Int = tasks.size

    private fun deleteTask(position: Int) {
        tasks.removeAt(position)
        notifyItemRemoved(position)
    }
}
