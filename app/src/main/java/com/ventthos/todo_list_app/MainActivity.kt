package com.ventthos.todo_list_app

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ventthos.todo_list_app.db.AppDatabase.AppDatabase
import com.ventthos.todo_list_app.db.dataclasses.Task
import com.ventthos.todo_list_app.db.dataclasses.TaskList

interface OnTaskCheckedChangeListener {
    fun onTaskCheckedChanged(task: Task, isChecked: Boolean)
}

interface OnTaskClickForEditListener{
    fun OnTaskClickForEdit(task: Task)
}

class MainActivity : AppCompatActivity(), TaskDialogFragment.TaskEditListener, ListDialogFragment.ListEditorListener, OnTaskCheckedChangeListener, OnTaskClickForEditListener {
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var drawerToggle: ActionBarDrawerToggle
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var fab: FloatingActionButton
    lateinit var recyclerView: RecyclerView
    lateinit var pageTitle: TextView
    lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var Firebase: Firebasesito
    private val taskModel: TaskModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Logica firebase
        //Firebase = Firebasesito()
        //Firebase.createUser("Vivi123","Victor", "Vivi","pass")
        //var lista = ""
        //Firebase.addList("Vivi123", "Lista", "1", 0) { listId ->
        //    if (listId != null) {
        //        lista = listId
        //        Log.d("Firestore", "Lista creada con ID: $listId")
        //    } else {
        //        Log.e("Firestore", "Error al crear la lista")
        //    }

        //}
        //var tarea = ""
        //Firebase.addTask("Vivi123", lista,"Tarea1", "Hacer tarea 2", 3, "2025-10-1", 1) {
        //    taskId ->
        //    if (taskId != null) {
        //        tarea = taskId
        //        Log.d("Firestore", "Lista creada con ID: $taskId")
        //    } else {
        //        Log.e("Firestore", "Error al crear la lista")
        //    }
        //}

        //Firebase.addTask("Vivi123", lista, "Tarea1", "Añadir tarea 2", 1, "2025-6-10", 1 )

        //termina logica firebase
        //Logica db
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "library"

        )
            .allowMainThreadQueries()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    db.execSQL("INSERT INTO user(id, name, lastName, email, avatar) VALUES (1,'Victor', 'Ez Calante', 'correo@dominio.com', 1)")

                }
            })
            .build()
        val userDao = db.UserDao()
        val taskListDao = db.TaskListDao()
        val taskDao = db.TaskDao()

        val users = userDao.getAllUsers()
        val taskLists = taskListDao.getAllUsersList(1)
        val insertTaskListId = taskLists.size + 1
        taskListDao.addList(TaskList(insertTaskListId,"Tareas",1,"time",-1,users[0].id))
        val tasks = taskDao.getAllListTasks(insertTaskListId)
        val insertTaskId = tasks.size + 1
        taskDao.addTask(Task(insertTaskId,"Hacer la db", "No se como aa", 3,"2025-06-25",false, insertTaskListId,1))


        //termina logica db
        //Logica del reciclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        taskModel.taskAdapter = ItemAdapter(taskModel.filteredTasks.toMutableList(), this, this)
        recyclerView.adapter = taskModel.taskAdapter
        //termina logia del recicler view

        window.statusBarColor = ContextCompat.getColor(this, R.color.mainColor)

        // find views
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)
        fab = findViewById(R.id.fab)
        pageTitle = findViewById(R.id.pageTitle)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)

        setSupportActionBar(findViewById(R.id.toolbar))

        // Drawer configuration
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawerDesc, R.string.closeDrawerDesc)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        // Listeners
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navAddList->{
                    ListDialogFragment().show(supportFragmentManager, "List")
                }
                R.id.nav_all->{
                    taskModel.currentPage = -1
                }
                R.id.nav_importants->{
                    taskModel.currentPage = -2
                }
                R.id.nav_planned->{
                    taskModel.currentPage = -3
                }
                R.id.nav_completed->{
                    taskModel.currentPage = -4
                }
                else->{
                    taskModel.currentPage = menuItem.itemId
                }
            }
            changePageStyles()
            runFilters()
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        fab.setOnClickListener{
            TaskDialogFragment().show(supportFragmentManager, "Task")
        }

        taskModel.getListFromDb(this)

        runFilters()
        redrawLists()
    }
    fun changePageStyles(){
        when (taskModel.currentPage) {
            -1 ->{
                pageTitle.setText(R.string.allVista)
            }
            -2 ->{
                pageTitle.setText(R.string.importantVista)
            }
            -3 ->{
                pageTitle.setText(R.string.plannedVista)
            }
            -4 -> {
                pageTitle.setText(R.string.completedVista)
            }
            else->{
                val list = taskModel.lists.first { it.id == taskModel.currentPage }
                pageTitle.text = list.name
            }
        }

        if(taskModel.currentPage < 0 && toolbar.menu.hasVisibleItems()){

            for (i in 0 until toolbar.menu.size()) {
                toolbar.menu.getItem(i).isVisible = false
            }
            fab.visibility = View.GONE
            return
        }
        else if(taskModel.currentPage >= 0 && !toolbar.menu.hasVisibleItems()){
            for (i in 0 until toolbar.menu.size()) {
                toolbar.menu.getItem(i).isVisible = true
            }
            fab.visibility = View.VISIBLE
        }

    }

    override fun onTaskEdit(
        id: Int,
        title: String,
        notes: String,
        importance: Int,
        date: String,
        editing: Boolean
    ) {
        if(!editing){
            taskModel.createTask(title, notes, importance, date, taskModel.currentPage)
        }
        else{
            taskModel.editTask(id, title, notes, importance,date)
        }
        runFilters()
    }

    override fun onListEdited(id: Int, title: String, icon: Int, colorId: Int, editing: Boolean) {
        if(!editing){
            taskModel.createList(title, icon, colorId, this)
            taskModel.getListFromDb(this)
            redrawLists()
        }
        else{
            Log.i("Me dan:", colorId.toString())
            taskModel.editList(id, title, icon, colorId, this)
            taskModel.getListFromDb(this)
            redrawLists()
        }
    }

    fun redrawLists(){
        val listsMenu = navigationView.menu.findItem(R.id.listMenuDisplay).subMenu
        listsMenu?.clear()
        for (list in taskModel.lists){
            val item = listsMenu?.add(0,list.id.hashCode(), 0, list.name)
            item?.setIcon(list.iconId)
        }
    }

    fun runFilters(){
        when (taskModel.currentPage) {
            -1 ->{
                taskModel.clearFilters(recyclerView)
            }
            -2 ->{
                taskModel.filtrateImportants(recyclerView)
            }
            -3 ->{
                taskModel.filtratePlanned(recyclerView)
            }
            -4 -> {
                taskModel.filtrateCompleted(recyclerView)
            }
            else ->{
                taskModel.filterByList(recyclerView)
            }
        }

    }

    override fun onTaskCheckedChanged(task: Task, isChecked: Boolean) {
        taskModel.changeCompleted(task.id.toInt(), isChecked)
        runFilters()
        if(isChecked){
            val snackbar = Snackbar.make(coordinatorLayout, R.string.completedConfirmation, Snackbar.LENGTH_LONG)
                .setAction(R.string.cancel){onTaskCheckedChanged(task, false)}
            snackbar.show()
        }
    }

    override fun OnTaskClickForEdit(task: Task) {
        TaskDialogFragment.setArguments(task.id.toInt(), task.title, task.notes, task.importance, task.date)
            .show(supportFragmentManager, "TaskEdit")

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.lista_menu, menu)
        changePageStyles()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.editList_nav->{
            val list = taskModel.lists.first { it.id == taskModel.currentPage }
            ListDialogFragment.setArguments(list.id.hashCode(), list.name, list.iconId, list.color).show(supportFragmentManager,"EditList")
            true
        }

        else -> super.onOptionsItemSelected(item)
    }
}