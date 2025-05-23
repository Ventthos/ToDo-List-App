package com.ventthos.todo_list_app

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Spinner
import androidx.core.util.Function
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.ventthos.todo_list_app.db.Daos.TaskDao
import com.ventthos.todo_list_app.db.Daos.TaskListDao
import com.ventthos.todo_list_app.db.Daos.UserDao
import com.ventthos.todo_list_app.db.dataclasses.TaskList
import com.ventthos.todo_list_app.db.dataclasses.Task
import com.google.firebase.database.GenericTypeIndicator
import com.ventthos.todo_list_app.db.dataclasses.TaskListFirebase

enum class SortOrder {
    DEFAULT,
    IMPORTANCE_ASC,
    IMPORTANCE_DESC,
    DATE_ASC,
    DATE_DESC
}

class TaskModel: ViewModel() {

    var currentUserId = ""

    var currentSortOrder: SortOrder = SortOrder.DEFAULT
    var onlyCompleted = true

    lateinit var taskDao: TaskDao
    lateinit var listDao: TaskListDao
    lateinit var userDao: UserDao


    //Aqui jalariamos los elementos de la base de datos para meterlos a una lista
    private val tasks = mutableListOf<Task>()

    var filteredTasks: MutableList<Task> = tasks

    val lists = mutableListOf<TaskList>()

    lateinit var taskAdapter: ItemAdapter

    var currentPage = -1
    var itemPosition = -1

    // Declaración de la db
    val database = Firebase.database

    fun getTasks(){
        tasks.clear()
        val userListsId = lists.map { it.id }
        tasks.addAll(taskDao.getAllTasksFromUser(userListsId))
        taskAdapter.notifyDataSetChanged()
    }

   fun createTask(title: String, notes: String, importance: Int, date: String?, listId: Int){
       var finalDate = date
        if (date == "")
            finalDate = null

       val colorId = lists.first { it.id == listId }.color
       getTasks()
       val newTask = Task(0, title, notes, importance, finalDate, false, listId, colorId)
        taskDao.addTask(newTask)

       getTasks()
        taskAdapter.notifyDataSetChanged()
   }


    fun editTask(id:Int, title: String, notes: String, importance: Int, date: String?) {
        var finaldate = date
        if (date == ""){
            finaldate = null
        }
        val task = taskDao.getTaskWithId(id)
        val updatedTask = task
        updatedTask.title = title
        updatedTask.notes = notes
        updatedTask.importance = importance
        updatedTask.date = finaldate

        task.colorId = lists.first { it.id == task.listId }.color
        taskDao.updateTask(updatedTask)

        getTasks()
        taskAdapter.notifyDataSetChanged()

    }
    fun deleteTask(id:Int, title: String, notes: String, importance: Int, date: String?) {

        var finalDate = date
        if (date == "") {
            finalDate = null
        }
        val TaskToDelete = Task(id,title,notes,importance,finalDate)
        taskDao.deleteTask(TaskToDelete)

        getTasks()
        taskAdapter.notifyDataSetChanged()
    }

    fun deleteSharedTask(id: String): Boolean{
        val list = sharedLists.firstOrNull { it.id == currentPage }
        if(list == null) return false

        database.getReference("lists").child(list.remoteId!!).child("tasks").child(id).removeValue()
        return true
    }

    fun clearFilters(recyclerView: RecyclerView){
        filteredTasks = tasks.filter {!it.completed }.toMutableList()
        taskAdapter.updateList(filteredTasks, recyclerView)
    }

    fun filtrateImportants(recyclerView: RecyclerView){
        filteredTasks = tasks.filter { it.importance > 0 && !it.completed }.sortedByDescending { it.importance }.toMutableList()
        taskAdapter.updateList(filteredTasks, recyclerView)

    }

    fun filtratePlanned(recyclerView: RecyclerView){
        filteredTasks = tasks.filter { it.date !== null && !it.completed}.sortedBy { it.date }.toMutableList()
        taskAdapter.updateList(filteredTasks,recyclerView)
    }

    fun filtrateCompleted(recyclerView: RecyclerView){
        filteredTasks = tasks.filter {it.completed}.toMutableList()
        taskAdapter.updateList(filteredTasks, recyclerView)
    }

    fun orderByImportance(descendingOrder: Boolean,recyclerView: RecyclerView){

        if (descendingOrder){
            filteredTasks.sortByDescending { it.importance }
            currentSortOrder = SortOrder.IMPORTANCE_DESC
        }
        else{
            filteredTasks.sortBy{ it.importance }
            currentSortOrder = SortOrder.IMPORTANCE_ASC
        }
        taskAdapter.updateList(filteredTasks, recyclerView)
    }

    fun orderByDate(descendingOrder: Boolean = true,recyclerView: RecyclerView){
        if(descendingOrder){
            filteredTasks.sortByDescending { it.date }
            currentSortOrder = SortOrder.DATE_DESC
        }
        else{
            filteredTasks.sortBy{ it.date }
            currentSortOrder = SortOrder.DATE_ASC
        }
        taskAdapter.updateList(filteredTasks, recyclerView)
    }

    fun setCompletedVisibility(hide: Boolean = true, recyclerView: RecyclerView){
        var tasksVisibility = filteredTasks.filter { it.listId == currentPage}.toMutableList()
        if(hide){
            tasksVisibility = tasksVisibility.filter { !it.completed }.toMutableList()
        }

        taskAdapter.updateList(tasksVisibility, recyclerView)
    }

    fun changeCompleted(id:Int, completed: Boolean){
        val task = tasks.first { it.id == id }
        task.completed = completed
        //agregar completa
        taskDao.updateTask(task)
        taskAdapter.notifyDataSetChanged()
    }

    fun changeCompletedShared(id: String, completed: Boolean): Boolean{
        val list = sharedLists.firstOrNull { it.id == currentPage }
        if(list == null) return false

        var stateRef = database.getReference("lists").child(list.remoteId!!).child("tasks").child(id).child("completed")
        stateRef.setValue(completed)
        taskAdapter.notifyDataSetChanged()
        return true
    }

    fun changeDateLimit(id: Int, date:String){
        taskDao.updateTaskLimit(id, date)
        getTasks()
        taskAdapter.notifyDataSetChanged()
    }

    fun getListFromDb(context: Context){
        lists.clear()
        lists.addAll(listDao.getAllUsersList(currentUserId))
        for (list in lists){
            val photoId = context.resources.getIdentifier(list.iconName, "drawable", context.packageName)
            list.iconId = photoId
        }
    }

    fun createList(title: String, icon: Int, colorId: Int, context: Context){
        val resourceName: String = context.resources.getResourceEntryName(icon)
        val newTask = TaskList(0, title, colorId, resourceName, icon, currentUserId)
        listDao.addList(newTask)
    }

    fun editList(id: Int, name: String, icon: Int, colorId: Int, context: Context){
        val resourceName: String = context.resources.getResourceEntryName(icon)
        val updatedList = TaskList(id, name, colorId, resourceName, icon, currentUserId)
        listDao.updateList(updatedList)
        taskDao.updateTaskListColorInTasks(updatedList.id, colorId)
        getTasks()
    }

    fun deleteList(id: Int, name: String, icon: Int, colorId: Int, context: Context){

        val resourceName: String = context.resources.getResourceEntryName(icon)
        val ListToDelete = TaskList(id, name, colorId, resourceName, icon, currentUserId)
        listDao.deleteList(ListToDelete)
        getTasks()

    }

    fun deleteSharedList(id: String){
        database.getReference("lists").child(id).removeValue()
        getTasks()
    }

    fun filterByList(recyclerView: RecyclerView, shared: Boolean = false){
        if(!shared){
            filteredTasks = tasks.filter { it.listId == currentPage }.toMutableList()
            taskAdapter.updateList(filteredTasks, recyclerView)
            return
        }
        val sharedTasks = sharedLists.firstOrNull { it.id == currentPage}?.tasks

        if(sharedTasks != null){
            filteredTasks = sharedTasks
            taskAdapter.updateList(filteredTasks, recyclerView)
            Log.i("YEA", filteredTasks.toString())
            return
        }
        else{
            currentPage = 0
            Log.i("ÑAO", "no se pudo encontrar")
        }
    }


    // Toda la lógica de Firebase

    // Aqui se guardan todas las listas del usuario que están en firebase
    var sharedLists = mutableListOf<TaskList>()
    // Filtramos por las listas que tiene el usuario o toDO por en las que está incluido
    val sharedListsRef = database.getReference("lists").orderByChild("id").equalTo(currentUserId)
    // Función para poder empezar a escuchar los cambios
    fun listenToSharedLists(updater: () -> Unit) {
        sharedListsRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Obtenemos cada una de los list lo mapeamos en un id local por problemas con
                // lo de que el drawer solo puede aceptar numeros

                sharedLists.clear()
                for (childSnapshot in snapshot.children) {
                    val firebaseId = childSnapshot.key
                    val firebaseTaskList = childSnapshot.getValue(TaskListFirebase::class.java)

                    if (firebaseId != null && firebaseTaskList != null) {
                        val hashcode = firebaseId.hashCode()
                        val fullTaskList = TaskList(
                            id = if (hashcode < 0) hashcode else -hashcode,
                            name = firebaseTaskList.name,
                            color = firebaseTaskList.color,
                            iconName = firebaseTaskList.iconName,
                            iconId = firebaseTaskList.iconId,
                            userId = firebaseTaskList.userId,
                        )

                        fullTaskList.remoteId = firebaseId

                        // Cargar tareas manualmente
                        val tasks = mutableListOf<Task>()
                        val tasksSnapshot = childSnapshot.child("tasks")
                        for (taskSnap in tasksSnapshot.children) {
                            val task = taskSnap.getValue(Task::class.java)
                            if (task != null) {
                                task.remoteId = taskSnap.key
                                tasks.add(task)
                            }
                        }
                        fullTaskList.tasks = tasks

                        sharedLists.add(fullTaskList)
                    }
                }

                updater() // llamada al callback, que es el actualizador de datos
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

}