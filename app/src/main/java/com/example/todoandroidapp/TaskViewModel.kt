package com.example.todoandroidapp

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> get() = _tasks
    private val _titles = MutableLiveData<List<String>>()
    val titles: LiveData<List<String>> get() = _titles

    init {
        val taskDao = AppDatabase.getDatabase(application).taskDao()
        val firebaseDatabase = FirebaseDatabase.getInstance().reference.child("tasks")
        repository = TaskRepository(taskDao, firebaseDatabase)
        loadDataFromDatabase()
        loadDataFromFirebase()
        fetchDataFromApi()
    }

    private fun fetchDataFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            val titles = repository.fetchDataFromApi()
            _titles.postValue(titles)
        }
    }

    private fun loadDataFromDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val tasksFromDb = repository.loadDataFromDatabase()
            _tasks.postValue(tasksFromDb)
        }
    }

    private fun loadDataFromFirebase() {
        viewModelScope.launch(Dispatchers.IO) {
            val tasksFromFirebase = repository.loadDataFromFirebase()
            _tasks.postValue(tasksFromFirebase)
        }
    }

    fun insertTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTask(task)
            loadDataFromDatabase()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTask(task)
            loadDataFromDatabase()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(task)
            loadDataFromDatabase()
        }
    }
}
