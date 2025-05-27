package com.ventthos.todo_list_app.db.dataclasses

import java.io.Serializable

data class UserFromSharedList(
    val remoteId: String,
    val name: String,
    val lastName: String,
    val email:String,
    var avatar: Int,
    var state: String,
    val avatarName: String = "mark"
): Serializable
