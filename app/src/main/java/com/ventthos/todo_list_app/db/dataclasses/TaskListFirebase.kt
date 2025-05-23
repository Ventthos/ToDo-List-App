package com.ventthos.todo_list_app.db.dataclasses

// Miren esto solo sirve par apoder parsear la lista directamente de Firebase. No se usa en nada más
data class TaskListFirebase(
    var id: Int = 0,
    var name: String = "",
    var color: Int = 0,
    var iconName: String = "",
    var iconId: Int = 0,
    var userId: Int = -1,
)