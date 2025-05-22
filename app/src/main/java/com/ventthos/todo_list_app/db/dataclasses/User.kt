package com.ventthos.todo_list_app.db.dataclasses

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val lastName: String,
    @ColumnInfo(name = "email") val email: String,
    val password: String,

    var avatar: Int,
    var lastPage: Int = -1
)