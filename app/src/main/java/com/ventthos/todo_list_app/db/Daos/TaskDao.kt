package com.ventthos.todo_list_app.db.Daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ventthos.todo_list_app.db.dataclasses.Task

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addTask(task: Task)

    @Query("SELECT * FROM task WHERE listId = :id")
    fun getAllListTasks(id : Int) : List<Task>

    @Delete
    fun deleteTask(task: Task)
}