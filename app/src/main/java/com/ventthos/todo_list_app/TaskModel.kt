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
    private val tasks = mutableListOf(
        Task(id = 1, title = "Comprar leche", notes = "Ir al supermercado", importance = 2, date = "2025-03-24", listId = 1, colorId = 0),
        Task(id = 2, title = "Estudiar Kotlin", notes = "Repasar funciones de extensión y lambdas", importance = 3, date = "2025-03-25", listId = 1, colorId = 0),
        Task(id = 3, title = "Llamar a mamá", notes = "Preguntar cómo está", importance = 1, date = "2025-03-24", listId = 2, colorId = 1),
        Task(id = 4, title = "Preparar presentación", notes = "Revisar las diapositivas", importance = 3, date = "2025-03-26", listId = 1, colorId = 0),
        Task(id = 5, title = "Hacer ejercicio", notes = "Correr 5 km en el parque", importance = 2, date = "2025-03-27", listId = 1, colorId = 0),
        Task(id = 6, title = "Pagar factura de luz", notes = "Realizar pago online", importance = 3, date = "2025-03-28", listId = 1, colorId = 0),
        Task(id = 7, title = "Comprar regalo de cumpleaños", notes = "Buscar algo original", importance = 3, date = "2025-03-30", listId = 1, colorId = 0),
        Task(id = 8, title = "Revisar emails", notes = "Responder a correos urgentes", importance = 2, date = "2025-03-24", listId = 1, colorId = 0),
        Task(id = 9, title = "Ir al médico", notes = "Chequeo general anual", importance = 3, date = "2025-03-29", listId = 1, colorId = 0),
        Task(id = 10, title = "Limpiar la casa", notes = "Aspirar y ordenar las habitaciones", importance = 1, date = "2025-03-25", listId = 1, colorId = 0)
    )

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

        val updatedTask = Task(id, title, notes, importance, date)


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

    fun filterByList(recyclerView: RecyclerView){
        filteredTasks = tasks.filter { it.listId == currentPage }.toMutableList()
        taskAdapter.updateList(filteredTasks, recyclerView)
    }

}