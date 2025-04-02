package com.ventthos.todo_list_app

data class TaskList(val id: String, var name: String, var color: Int, var icon: Int)

data class TaskListDB(val id: Int, var name: String, var color: Int, var icon: String)
