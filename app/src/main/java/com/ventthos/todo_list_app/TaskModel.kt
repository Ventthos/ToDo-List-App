package com.ventthos.todo_list_app

import android.R.id
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView


//Clase para los items dentro de el recicler view
data class Task(val id: Int, var title: String, var notes: String = "", var importance: Int,
                var date: String?, var completed: Boolean = false, val listId: Int = 0, val colorId: Int = 0)

class TaskModel: ViewModel() {
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

    private val listDb = mutableListOf(
        TaskListDB(1, "Pendientes", 0, "time")
    )

    val lists = mutableListOf<TaskList>(

    )

    lateinit var taskAdapter: ItemAdapter

    var currentPage = -1

   fun createTask(title: String, notes: String, importance: Int, date: String?, listId: Int){
       var finalDate = date
        if (date == "")
            finalDate = null

       val colorId = lists.first { it.id == listId }.color
       val newTask = Task(tasks.size+1, title, notes, importance, finalDate, false, listId, colorId)


        tasks.add(newTask)
        taskAdapter.notifyDataSetChanged()
   }

    fun getChangedFieldsInTask(original: Task, updated: Task): Map<String, Any?> {
        val changes = mutableMapOf<String, Any?>()

        if (original.title != updated.title) changes["title"] = updated.title
        if (original.notes != updated.notes) changes["notes"] = updated.notes
        if (original.importance != updated.importance) changes["importance"] = updated.importance
        if (original.date != updated.date) changes["date"] = updated.date
        return changes
    }

    fun editTask(id:Int, title: String, notes: String, importance: Int, date: String?) {
        val task = tasks.first { it.id == id }
        val updatedTask = Task(id, title, notes, importance, date)

        val changes = getChangedFieldsInTask(task, updatedTask)
        Log.i("CAMBIOS HAY EN MIII", changes.toString())
        changes.forEach { (key, value) ->
            when (key) {
                "title" -> task.title = value as String
                "notes" -> task.notes = value as String
                "importance" -> task.importance = value as Int
                "date" -> task.date = value as String?
                "completed" -> task.completed = value as Boolean
            }
        }
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

    fun createList(title: String, icon: Int, colorId: Int, context: Context){
        val resourceName: String = context.resources.getResourceEntryName(icon)
        listDb.add(
            TaskListDB(lists.size+1, title, colorId, resourceName)
        )
    }

    fun getChangedFieldsInList(original: TaskList, updated: TaskList): Map<String, Any?> {
        val changes = mutableMapOf<String, Any?>()

        if (original.name != updated.name) changes["name"] = updated.name
        if (original.icon != updated.icon) changes["icon"] = updated.icon
        if (original.color != updated.color) changes["color"] = updated.color
        return changes
    }

    fun editList(id: Int, name: String, icon: Int, colorId: Int, context: Context){
        val oldList = lists.first { it.id == id }
        val updatedList = TaskList(id, name, colorId, icon)

        val listDb = listDb.first { it.id == id }

        val changes = getChangedFieldsInList(oldList, updatedList)
        Log.i("CAMBIOS HAY EN MIII", changes.toString())
        val resourceName: String = context.resources.getResourceEntryName(icon)

        changes.forEach { (key, value) ->
            when (key) {
                "name" -> listDb.name = value as String
                "icon" -> listDb.icon = resourceName
                "color" -> listDb.color = value as Int
            }
        }
    }

    fun getListFromDb(context: Context){
        lists.clear()
        for (x in listDb){
            val photoId = context.resources.getIdentifier(x.icon, "drawable", context.packageName)
            lists.add(TaskList(x.id, x.name, x.color, photoId))
        }
    }

    fun filterByList(recyclerView: RecyclerView){
        filteredTasks = tasks.filter { it.listId == currentPage }.toMutableList()
        taskAdapter.updateList(filteredTasks, recyclerView)
    }

}