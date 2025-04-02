package com.ventthos.todo_list_app.db.AppDatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ventthos.todo_list_app.db.Daos.TaskDao
import com.ventthos.todo_list_app.db.Daos.TaskListDao
import com.ventthos.todo_list_app.db.Daos.UserDao
import com.ventthos.todo_list_app.db.dataclasses.Task
import com.ventthos.todo_list_app.db.dataclasses.TaskList
import com.ventthos.todo_list_app.db.dataclasses.User

@Database(entities = [User::class, Task::class, TaskList::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun UserDao() : UserDao
    abstract fun TaskListDao() : TaskListDao
    abstract fun TaskDao() : TaskDao
}