package com.ventthos.todo_list_app.db.Daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ventthos.todo_list_app.db.dataclasses.TaskList

@Dao
interface TaskListDao {
    @Query("SELECT * FROM list WHERE userId = :id")
    fun getAllUsersList(id : String) : MutableList<TaskList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addList(list: TaskList)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateList(list: TaskList)

    @Delete
    fun deleteList(list: TaskList)
}