package com.ventthos.todo_list_app.db.dataclasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey var id :Int,
    var name:String,
    var lastName:String,
    var email:String?,
    var avatar:Int
)