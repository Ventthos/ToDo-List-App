package com.ventthos.todo_list_app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isEmpty
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

interface OnTaskCheckedChangeListener {
    fun onTaskCheckedChanged(task: Task, isChecked: Boolean)
}
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = ""
)


class MainActivity : AppCompatActivity(), TaskDialogFragment.TaskEditListener, ListDialogFragment.ListEditorListener, OnTaskCheckedChangeListener {
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

    private val taskModel: TaskModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Logica firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        saveUser()
        //termina logica firebase

        //Logica del reciclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        taskModel.taskAdapter = ItemAdapter(taskModel.filteredTasks.toMutableList(), this)
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

        changePageStyles()
        redrawLists()
    }
    fun saveUser() {
        val db = FirebaseFirestore.getInstance()
        val user = User(id = "1", name = "Víctor", email = "victor@gmail.com")

        db.collection("users") // Nombre de la colección
            .document(user.id) // ID del documento
            .set(user) // Guardar el objeto
            .addOnSuccessListener {
                println("Usuario guardado correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al guardar usuario: ${e.message}")
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

        if(taskModel.currentPage <0 && toolbar.menu.hasVisibleItems()){
            toolbar.menu.clear()
            fab.visibility = View.GONE
            return
        }
        else if(taskModel.currentPage >=0 && !toolbar.menu.hasVisibleItems()){
            toolbar.inflateMenu(R.menu.lista_menu)
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
        runFilters()
    }

    override fun onListEdited(id: Int, title: String, icon: Int, colorId: Int, editing: Boolean) {
        if(!editing){
            taskModel.createList(title, icon, colorId, this)
            taskModel.getListFromDb(this)
            redrawLists()
        }
    }

    fun redrawLists(){
        val listsMenu = navigationView.menu.findItem(R.id.listMenuDisplay).subMenu
        listsMenu?.clear()
        for (list in taskModel.lists){
            val item = listsMenu?.add(0,list.id, 0, list.name)
            item?.setIcon(list.icon)
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
        taskModel.changeCompleted(task.id, isChecked)
        runFilters()
        if(isChecked){
            val snackbar = Snackbar.make(coordinatorLayout, R.string.completedConfirmation, Snackbar.LENGTH_LONG)
                .setAction(R.string.cancel){onTaskCheckedChanged(task, false)}
            snackbar.show()
        }


    }
}