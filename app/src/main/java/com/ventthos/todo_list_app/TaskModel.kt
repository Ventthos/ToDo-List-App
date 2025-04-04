package com.ventthos.todo_list_app

import android.content.Context
import android.util.Log
import android.widget.Spinner
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.ventthos.todo_list_app.db.Daos.TaskDao
import com.ventthos.todo_list_app.db.Daos.TaskListDao
import com.ventthos.todo_list_app.db.Daos.UserDao
import com.ventthos.todo_list_app.db.dataclasses.TaskList
import com.ventthos.todo_list_app.db.dataclasses.Task

enum class SortOrder {
    DEFAULT,
    IMPORTANCE_ASC,
    IMPORTANCE_DESC,
    DATE_ASC,
    DATE_DESC
}

class TaskModel: ViewModel() {

    var currentUserId = -1

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
        Log.i("Oyee", "Me estan pasando ${id}")
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

    fun changeDateLimit(id: Int, date:String){
        taskDao.updateTaskLimit(id, date)
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