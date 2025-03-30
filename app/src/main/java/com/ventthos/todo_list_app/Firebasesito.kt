package com.ventthos.todo_list_app

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

data class User(
    val name: String = "",
    val username: String = "",
    val password: String = ""
)

data class ListItem(
    val name: String = ""
)

class Firebasesito {

    fun createUser(userId: String, name: String, username: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        val user = User(name, username, password)

        db.collection("users")
            .document(userId) // El ID del usuario (podría ser su UID de Firebase Auth)
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "Usuario creado con éxito")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al crear usuario", e)
            }
    }
    fun getUsers(callback: (List<User>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { it.toObject(User::class.java) }
                callback(users)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al obtener usuarios", e)
            }
    }

    fun addList(userId: String, listName: String) {
        val db = FirebaseFirestore.getInstance()
        val newList = ListItem(listName)

        db.collection("users").document(userId)
            .collection("lists")
            .add(newList)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Lista creada con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al agregar lista", e)
            }
    }
    fun addTask(userId: String, listId: String, name: String, startDate: String, endDate: String, priority: String) {
        val db = FirebaseFirestore.getInstance()
        val newTask = Task(name, startDate, endDate, priority)
        Task(id = 1, title = "Comprar leche", notes = "Ir al supermercado", importance = 2, date = "2025-03-24", listId = 1, colorId = 0)

        db.collection("users").document(userId)
            .collection("lists").document(listId)
            .collection("tasks")
            .add(newTask)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Tarea creada con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al agregar tarea", e)
            }
    }
    fun getLists(userId: String, callback: (List<ListItem>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .collection("lists")
            .get()
            .addOnSuccessListener { result ->
                val lists = result.documents.mapNotNull { it.toObject(ListItem::class.java) }
                callback(lists)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al obtener listas", e)
            }
    }
    fun getTasks(userId: String, listId: String, callback: (List<Task>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .collection("lists").document(listId)
            .collection("tasks")
            .get()
            .addOnSuccessListener { result ->
                val tasks = result.documents.mapNotNull { it.toObject(Task::class.java) }
                callback(tasks)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al obtener tareas", e)
            }
    }
    fun deleteList(userId: String, listId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .collection("lists").document(listId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Lista eliminada con éxito")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al eliminar lista", e)
            }
    }
    fun deleteTask(userId: String, listId: String, taskId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .collection("lists").document(listId)
            .collection("tasks").document(taskId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Tarea eliminada con éxito")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al eliminar tarea", e)
            }
    }

}
