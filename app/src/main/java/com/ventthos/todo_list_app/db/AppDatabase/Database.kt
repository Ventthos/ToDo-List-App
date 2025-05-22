package com.ventthos.todo_list_app.db.AppDatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ventthos.todo_list_app.db.Daos.TaskDao
import com.ventthos.todo_list_app.db.Daos.TaskListDao
import com.ventthos.todo_list_app.db.Daos.UserDao
import com.ventthos.todo_list_app.db.Daos.SessionDao
import com.ventthos.todo_list_app.db.dataclasses.Task
import com.ventthos.todo_list_app.db.dataclasses.TaskList
import com.ventthos.todo_list_app.db.dataclasses.User
import com.ventthos.todo_list_app.db.dataclasses.Session

@Database(entities = [User::class, Task::class, TaskList::class, Session::class], version = 11)
abstract class AppDatabase : RoomDatabase() {
    abstract fun UserDao() : UserDao
    abstract fun TaskListDao() : TaskListDao
    abstract fun TaskDao() : TaskDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todo_list_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

