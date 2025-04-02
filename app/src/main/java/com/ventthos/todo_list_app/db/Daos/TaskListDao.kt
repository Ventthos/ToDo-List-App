package com.ventthos.todo_list_app.db.Daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ventthos.todo_list_app.db.dataclasses.TaskList

@Dao
interface TaskListDao {
    @Query("SELECT * FROM list WHERE userId = :id")
    fun getAllUsersList(id : Int) : List<TaskList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addList(list: TaskList)
}