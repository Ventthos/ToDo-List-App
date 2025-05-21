package com.ventthos.todo_list_app

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
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
import androidx.room.util.query
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.ventthos.todo_list_app.db.AppDatabase.AppDatabase
import com.ventthos.todo_list_app.db.dataclasses.Task
import com.ventthos.todo_list_app.db.dataclasses.TaskList
import android.widget.Button
import android.content.Intent
import android.widget.ImageView
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import java.util.Locale


interface OnTaskCheckedChangeListener {
    fun onTaskCheckedChanged(task: Task, isChecked: Boolean)
}

interface OnTaskClickForEditListener{
    fun OnTaskClickForEdit(task: Task)
}


class MainActivity : AppCompatActivity(), TaskDialogFragment.TaskEditListener, ListDialogFragment.ListEditorListener, OnTaskCheckedChangeListener, OnTaskClickForEditListener , DateDialogFragment.DatePickerListener {
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var drawerToggle: ActionBarDrawerToggle
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var fab: FloatingActionButton
    lateinit var recyclerView: RecyclerView
    lateinit var pageTitle: TextView
    lateinit var coordinatorLayout: CoordinatorLayout
    private val taskModel: TaskModel by viewModels()
    lateinit var database: FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Logica db
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "todo_list_database"
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                   
                }
            })
            .build()
        taskModel.userDao = db.UserDao()
        taskModel.listDao = db.TaskListDao()
        taskModel.taskDao = db.TaskDao()

        /*
        val users = userDao.getAllUsers()
        val taskLists = taskListDao.getAllUsersList(1)
        val insertTaskListId = taskLists.size + 1
        taskListDao.addList(TaskList(insertTaskListId,"Tareas",1,"time",-1,users[0].id))
        val tasks = taskDao.getAllListTasks(insertTaskListId)
        val insertTaskId = tasks.size + 1
        taskDao.addTask(Task(insertTaskId,"Hacer la db", "No se como aa", 3,"2025-06-25",false, insertTaskListId,1))
        */
        val sessionDao = db.sessionDao()

        val session = sessionDao.getActiveSession()
        val userId: Int = if (intent.hasExtra("userId")) {
            intent.getIntExtra("userId", -1)
        } else {
            session?.userId ?: -1
        }

        if (userId != -1) {
            taskModel.currentUserId = userId
            val currentUser = taskModel.userDao.getUserById(userId)
            taskModel.currentPage = currentUser?.lastPage ?: -1 // Se restaura el lastPage
            if (savedInstanceState != null) {
                taskModel.currentPage = savedInstanceState.getInt("currentPage", taskModel.currentPage)
            }
        } else {
            Toast.makeText(this, "No hay sesión activa. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }


        //termina logica db

        // Declaración de variables de Firebase
        database = Firebase.database


        //Logica del reciclerView

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        taskModel.taskAdapter = ItemAdapter(taskModel.filteredTasks.toMutableList(), this, this, this)
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

        val headerView = navigationView.getHeaderView(0)

        // Mostramos nombre, correo y avatar del usuario
        val nameTextView = headerView.findViewById<TextView>(R.id.nav_header_name)
        val emailTextView = headerView.findViewById<TextView>(R.id.nav_header_email)
        val avatarImageView = headerView.findViewById<ImageView>(R.id.avatarImageView)

        val currentUser = taskModel.userDao.getUserById(taskModel.currentUserId)
        if (currentUser != null) {
            nameTextView.text = "${currentUser.name} ${currentUser.lastName}"
            emailTextView.text = currentUser.email ?: ""

            val avatarResId = if (currentUser.avatar != 0) currentUser.avatar else R.drawable.mark
            avatarImageView.setImageResource(avatarResId)
        }

        // Drawer configuration
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawerDesc, R.string.closeDrawerDesc)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        // Listeners
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                // Click para agregar listas normales
                R.id.navAddList->{
                    ListDialogFragment().show(supportFragmentManager, "List")
                }
                // Click para agregar listas de la nube
                R.id.navAddSharedList->{
                    ListDialogFragment.createSharedList().show(supportFragmentManager, "List")
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
                R.id.nav_logout -> {
                    Thread {
                        val db = AppDatabase.getDatabase(this)

                        // Actualiza el lastPage antes de limpiar la sesión
                        val currentUser = taskModel.userDao.getUserById(taskModel.currentUserId)
                        currentUser?.let {
                            taskModel.userDao.updateLastPage(currentUser.id, taskModel.currentPage)
                        }

                        db.sessionDao().clearSession()

                        runOnUiThread {
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }.start()
                    true
                }

                else->{
                    taskModel.currentPage = menuItem.itemId
                }
            }

            sessionDao.updateCurrentPageForUser(taskModel.currentUserId, taskModel.currentPage)

            changePageStyles()
            runFilters(true)
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        fab.setOnClickListener{
            TaskDialogFragment().show(supportFragmentManager, "Task")
        }

        taskModel.getListFromDb(this)
        taskModel.getTasks()
        runFilters()
        redrawLists()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentPage", taskModel.currentPage)
    }

    override fun onPause() {
        super.onPause()
        val currentUser = taskModel.userDao.getUserById(taskModel.currentUserId)
        currentUser?.let {
            taskModel.userDao.updateLastPage(it.id, taskModel.currentPage)
        }
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
        taskModel.getListFromDb(this)
        redrawLists()
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
        }
        else{
            Log.i("Me dan:", colorId.toString())
            taskModel.editList(id, title, icon, colorId, this)
        }
        taskModel.getListFromDb(this)
        redrawLists()
        runFilters()
    }

    override fun onListDeleted(id: Int, title: String, icon: Int, colorId: Int, editing: Boolean) {
        taskModel.deleteList(id, title, icon, colorId, this)
        taskModel.getListFromDb(this)
        redrawLists()
        taskModel.currentPage = -1
        runFilters()
    }

    override fun onSharedListEdited(
        id: String,
        title: String,
        icon: Int,
        colorId: Int,
        editing: Boolean
    ) {
        if(!editing){
            val lists = database.getReference("lists")
            lists.push().setValue(TaskList(-1, title, colorId, "", icon, taskModel.currentUserId))
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

    fun runFilters(goDetault:Boolean = false){
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

        if(goDetault){
            taskModel.currentSortOrder = SortOrder.DEFAULT
            taskModel.onlyCompleted = true
        }
        if (taskModel.currentPage >= 0)
            runOrder(taskModel.currentSortOrder)
    }

    override fun onTaskCheckedChanged(task: Task, isChecked: Boolean) {
        taskModel.changeCompleted(task.id, isChecked)
        runFilters()
        if(isChecked){
            val snackbar = Snackbar.make(coordinatorLayout, R.string.completedConfirmation, Snackbar.LENGTH_LONG)
                .setAction(R.string.cancel){onTaskCheckedChanged(task, false)}
            snackbar.show()
        }
    }

    override fun OnTaskClickForEdit(task: Task) {
        TaskDialogFragment.setArguments(task.id, task.title, task.notes, task.importance, task.date)
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
        R.id.order_by_importance_descending_menu->{
            taskModel.currentSortOrder = SortOrder.IMPORTANCE_DESC
            runOrder(taskModel.currentSortOrder)
            true
        }
        R.id.order_by_importance_ascending_menu->{
            taskModel.currentSortOrder = SortOrder.IMPORTANCE_ASC
            runOrder(taskModel.currentSortOrder)
            true
        }
        R.id.order_by_date_descending_menu->{
            taskModel.currentSortOrder = SortOrder.DATE_DESC
            runOrder(taskModel.currentSortOrder)
            true
        }
        R.id.order_by_date_ascending_menu->{
            taskModel.currentSortOrder = SortOrder.DATE_ASC
            runOrder(taskModel.currentSortOrder)
            true
        }
        R.id.show_completed_menu->{
            taskModel.onlyCompleted = !taskModel.onlyCompleted
            taskModel.setCompletedVisibility(taskModel.onlyCompleted, recyclerView)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    fun runOrder(order: SortOrder){
        when(order){
            SortOrder.IMPORTANCE_DESC->taskModel.orderByImportance(true, recyclerView)
            SortOrder.IMPORTANCE_ASC -> taskModel.orderByImportance(false, recyclerView)
            SortOrder.DATE_DESC -> taskModel.orderByDate(true, recyclerView)
            SortOrder.DATE_ASC ->  taskModel.orderByDate(false, recyclerView)
            SortOrder.DEFAULT -> {}
        }
        taskModel.setCompletedVisibility(taskModel.onlyCompleted, recyclerView)
    }

    // Esto es del context menu
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        // Se infla el menu
        menuInflater.inflate(R.menu.context_menu, menu)

        // Y vamos obteniendo la posición cada que den clic sobre un objeto
        val vh = recyclerView.getChildViewHolder(v!!) as ItemAdapter.ItemViewHolder
        taskModel.itemPosition = vh.adapterPosition
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        // Si nos deuvelve algo inválido cancelamos todo
        if (taskModel.itemPosition == -1) return super.onContextItemSelected(item)

        val task = taskModel.taskAdapter.itemList[taskModel.itemPosition] // Obtiene la tarea seleccionada del viewHolder

        // Aquí checamos que opción agarraron
        return when (item.itemId) {
            //Si eligieron delete, pues llamamos a la función
            R.id.deleteActionMenu ->{
                taskModel.deleteTask(task.id, task.title, task.notes, task.importance, task.date)
                runFilters()
                true
            }
            //Aqui para cambiar fecha de vencimiento
            R.id.changeDateMenu ->{
                val dateDialog = DateDialogFragment()
                dateDialog.show(supportFragmentManager, "datePicker")
                true
            }
            R.id.removeImportanceMenu ->{ //Si eligieron eliminar importancia, llamamos a la función
                taskModel.editTask(task.id, task.title, task.notes, 0, task.date)
                runFilters()
                true
            }
            R.id.setTodayLimitMenu->{ // Settea la fecha de hoy
                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = formatter.format(System.currentTimeMillis())
                taskModel.changeDateLimit(task.id, today)
                runFilters()
                true
            }
            R.id.setTomorrowLimitMenu->{ // Settea la fecha de mañana
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1) // Suma un día
                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) // Formato de fecha
                val tomorrow = formatter.format(calendar.time)

                taskModel.changeDateLimit(task.id, tomorrow)
                runFilters()
                true
            }
            R.id.removeDateMenu->{ // Remueve la fecha
                taskModel.editTask(task.id, task.title, task.notes, task.importance, null)
                runFilters()
                true
            }
            R.id.completeActionMenu->{ // Completa la tarea
                taskModel.changeCompleted(task.id, true)
                runFilters()
                true
            }
            else->{
                false
            }
        }
    }


    override fun onDateSelected(year: Int, month: Int, day: Int) {
        // Parceamos los datos
        val finalDay = if(day < 10) "0${day}" else day
        val finalMonth = if(month < 10) "0${month}" else month
        val finalDate = "${year}-${finalMonth}-${finalDay}"
        Log.i("Fecha final", finalDate)
        val task = taskModel.taskAdapter.itemList[taskModel.itemPosition]
        taskModel.changeDateLimit(task.id, finalDate)
        runFilters()
    }
}