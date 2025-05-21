package com.ventthos.todo_list_app.db.dataclasses

import java.io.Serializable

data class UserFromSharedList(
    val remoteId: Int,
    val name: String,
    val lastName: String,
    var avatar: Int,
    var state: String
): Serializable
