package com.ventthos.todo_list_app.db.Daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ventthos.todo_list_app.db.dataclasses.Task

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addTask(task: Task)

    @Query("SELECT * FROM task")
    fun getAllTasks(): MutableList<Task>

    @Query("SELECT * FROM task WHERE listId = :id")
    fun getAllListTasks(id : Int) : List<Task>

    @Query("SELECT * FROM task WHERE listId IN (:ids)")
    fun getAllTasksFromUser(ids: List<Int>): MutableList<Task>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTask(task: Task)

    @Query("UPDATE task SET colorId = :colorId WHERE listId = :idList")
    fun updateTaskListColorInTasks(idList:Int, colorId:Int)

    @Delete
    fun deleteTask(task: Task)
}