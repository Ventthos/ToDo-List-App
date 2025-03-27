package com.ventthos.todo_list_app

import android.content.Context
import androidx.lifecycle.ViewModel

//Clase para los items dentro de el recicler view
data class Task(val id: Int, var title: String, var notes: String = "", var importance: Int,
                var date: String?, var completed: Boolean = false, var list: String? = null)

class TaskModel: ViewModel() {
    //Aqui jalariamos los elementos de la base de datos para meterlos a una lista
    val tasks = mutableListOf(
        Task(id = 1, title = "Comprar leche", notes = "Ir al supermercado", importance = 2, date = "2025-03-24"),
        Task(id = 2, title = "Estudiar Kotlin", notes = "Repasar funciones de extensión y lambdas", importance = 3, date = "2025-03-25"),
        Task(id = 3, title = "Llamar a mamá", notes = "Preguntar cómo está", importance = 1, date = "2025-03-24"),
        Task(id = 4, title = "Preparar presentación", notes = "Revisar las diapositivas", importance = 4, date = "2025-03-26"),
        Task(id = 5, title = "Hacer ejercicio", notes = "Correr 5 km en el parque", importance = 2, date = "2025-03-27"),
        Task(id = 6, title = "Pagar factura de luz", notes = "Realizar pago online", importance = 3, date = "2025-03-28"),
        Task(id = 7, title = "Comprar regalo de cumpleaños", notes = "Buscar algo original", importance = 4, date = "2025-03-30"),
        Task(id = 8, title = "Revisar emails", notes = "Responder a correos urgentes", importance = 2, date = "2025-03-24"),
        Task(id = 9, title = "Ir al médico", notes = "Chequeo general anual", importance = 3, date = "2025-03-29"),
        Task(id = 10, title = "Limpiar la casa", notes = "Aspirar y ordenar las habitaciones", importance = 1, date = "2025-03-25")
    )

    val listDb = mutableListOf(
        TaskListDB(1, "Pendientes", 1, "programated")
    )

    val lists = mutableListOf<TaskList>(

    )

    var taskAdapter = ItemAdapter(tasks)

   fun createTask(title: String, notes: String, importance: Int, date: String?){
       var finalDate = date
        if (date == "")
            finalDate = null

        val newTask = Task(tasks.size, title, notes, importance, finalDate)
        tasks.add(newTask)
        taskAdapter.notifyDataSetChanged()
    }

    fun getListFromDb(context: Context){
        lists.clear()
        for (x in listDb){
            val photoId = context.resources.getIdentifier(x.icon, "drawable", context.packageName)
            lists.add(TaskList(x.id, x.name, x.color, photoId))
        }
    }

    fun createList(title: String, icon: Int, colorId: Int){
        lists.add(
            TaskList(lists.size, title, colorId, icon)
        )
    }


}