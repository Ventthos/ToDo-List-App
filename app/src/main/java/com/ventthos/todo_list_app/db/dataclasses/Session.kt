package com.ventthos.todo_list_app.db.dataclasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int
)