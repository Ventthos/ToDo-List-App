package com.ventthos.todo_list_app.db.dataclasses

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "list",
    foreignKeys = [ForeignKey(
        entity = User::class,      // Hace referencia a la tabla `user`
        parentColumns = ["id"],    // Clave primaria en `User`
        childColumns = ["userId"], // Clave foránea en `ListEntity`
        onDelete = ForeignKey.CASCADE // Si un usuario se borra, también sus listas
    )],
    indices = [Index(value = ["userId"])] // Índice para mejorar rendimiento
)
data class TaskList(
    @PrimaryKey (autoGenerate = true) var id: Int = 0,
    var name: String = "",
    var color: Int = 0,
    var iconName :String = "",
    var iconId: Int = 0,
    var userId :Int = -1,
)
{
    // Firebase cosas
    @Ignore var remoteId: String? = null
    @Ignore var sharedUsers: MutableList<UserFromSharedList>? = null
    @Ignore var tasks: MutableList<Task>? = null
}


