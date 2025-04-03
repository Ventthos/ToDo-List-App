package com.ventthos.todo_list_app

import android.util.Log
import com.ventthos.todo_list_app.db.dataclasses.Task

data class User(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val password: String = ""
)
data class ListItem(
    val name: String = "",
    val icon: String = "",
    val color: Int = 0
)

/*
class Firebasesito {


    fun createUser(userId: String, name: String, username: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        val user = User(userId,name, username, password)

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

    fun addList(userId: String, listName: String, icon: String, color: Int, callback: (String?) -> Unit){
        val db = FirebaseFirestore.getInstance()
        val newList = ListItem(listName, icon, color)
        db.collection("users").document(userId)
            .collection("lists")
            .add(newList)
            .addOnSuccessListener { documentReference ->
                val listId = documentReference.id
                Log.d("Firestore", "Lista creada con ID: ${documentReference.id}")
                callback(listId)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al agregar lista", e)
                callback(null)
            }
    }
    fun addTask(userId: Int, listId: Int, name: String,notas:String,importancia:Int, endDate: String, color: Int, callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val newTask = Task(id=userId, title=name,notes=notas, importance=importancia, date=endDate, listId=listId, colorId = color)

        db.collection("users").document(userId.toString())
            .collection("lists").document(listId.toString())
            .collection("tasks")
            .add(newTask)
            .addOnSuccessListener { documentReference ->
                val taskId =documentReference.id
                callback(taskId)
                Log.d("Firestore", "Tarea creada con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                callback(null)
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
*/