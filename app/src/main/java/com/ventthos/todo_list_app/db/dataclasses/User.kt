package com.ventthos.todo_list_app.db.dataclasses

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var name: String,
    var lastName: String,
    var email: String?,
    val password: String,
    var avatar: Int
)