package com.ventthos.todo_list_app

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.ventthos.todo_list_app.db.Daos.TaskDao
import com.ventthos.todo_list_app.db.Daos.TaskListDao
import com.ventthos.todo_list_app.db.Daos.UserDao
import com.ventthos.todo_list_app.db.dataclasses.TaskList
import com.ventthos.todo_list_app.db.dataclasses.Task

//Clase para los items dentro de el recicler view

class TaskModel: ViewModel() {

    var currentUserId = -1

    lateinit var taskDao: TaskDao
    lateinit var listDao: TaskListDao
    lateinit var userDao: UserDao

    //Aqui jalariamos los elementos de la base de datos para meterlos a una lista
    private val tasks = mutableListOf<Task>()

    var filteredTasks: MutableList<Task> = tasks

    val lists = mutableListOf<TaskList>()

    lateinit var taskAdapter: ItemAdapter

    var currentPage = -1

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

    fun changeCompleted(id:Int, completed: Boolean){
        val task = tasks.first { it.id == id }
        task.completed = completed
        //agregar completa
        taskDao.updateTask(task)
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

    fun filterByList(recyclerView: RecyclerView){
        filteredTasks = tasks.filter { it.listId == currentPage }.toMutableList()
        taskAdapter.updateList(filteredTasks, recyclerView)
    }

}