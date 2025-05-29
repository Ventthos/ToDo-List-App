package com.ventthos.todo_list_app

import android.content.ClipData
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
import androidx.appcompat.app.AlertDialog
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.ventthos.todo_list_app.db.dataclasses.UserFromSharedList
import java.util.Locale


interface OnTaskCheckedChangeListener {
    fun onTaskCheckedChanged(task: Task, isChecked: Boolean)
}

interface OnTaskClickForEditListener{
    fun OnTaskClickForEdit(task: Task)
}

data class PendingInvite(
    val fromUser: String = "",
    val listName: String = "",
    val timestamp: Long = 0L
)
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
            taskModel.currentUserEmail = currentUser?.email ?: ""
            taskModel.currentUserImage = currentUser?.avatar ?: R.drawable.mark
            taskModel.sharedListsRef = taskModel.database.getReference("lists").orderByChild("userId").equalTo(taskModel.currentUserId.toDouble())
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
        //invites
        revisarInvitacionesPendientes()


        //termina logica db


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
                    if (taskModel.currentPage < -4) {
                        val list = taskModel.sharedLists.firstOrNull{it.id == taskModel.currentPage}
                        if (list == null) {
                            Toast.makeText(this, "Womp womp", Toast.LENGTH_SHORT).show()

                        } else {
                            onSharedListClicked(list)
                        }
                    }
                }
            }

            sessionDao.updateCurrentPageForUser(taskModel.currentUserId, taskModel.currentPage)


            runFilters(true)
            changePageStyles()
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        fab.setOnClickListener{
            TaskDialogFragment().show(supportFragmentManager, "Task")
        }

        taskModel.getListFromDb(this)
        taskModel.getTasks()

        //Configuramos el escuchar las listas
        taskModel.listenToSharedLists( {
            runFilters()
            redrawLists()
            changePageStyles()
        },this)
        runFilters()
        redrawLists()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentPage", taskModel.currentPage)
    }
    override fun onResume() {
        super.onResume()
        revisarInvitacionesPendientes()
    }

    override fun onPause() {
        super.onPause()
        val currentUser = taskModel.userDao.getUserById(taskModel.currentUserId)
        currentUser?.let {
            taskModel.userDao.updateLastPage(it.id, taskModel.currentPage)
        }
    }

    private fun revisarInvitacionesPendientes() {
        val usersRef = Firebase.database.getReference("users")
        val currentEmail = taskModel.currentUserEmail

        usersRef.orderByChild("email").equalTo(currentEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return

                    val userFirebaseId = snapshot.children.first().key ?: return
                    val invitesRef = Firebase.database.getReference("pendingInvites").child(userFirebaseId)

                    invitesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (inviteSnap in snapshot.children) {
                                val invite = inviteSnap.getValue(PendingInvite::class.java)
                                val listId = inviteSnap.key ?: continue

                                val fromUser = invite?.fromUser ?: "Alguien"
                                val listName = invite?.listName ?: "una lista"

                                Snackbar.make(
                                    findViewById(R.id.coordinatorLayout),
                                    "$fromUser te ha invitado a '$listName'",
                                    Snackbar.LENGTH_LONG
                                )
                                    .setAction("Aceptar") {
                                        invitesRef.child(listId).removeValue()
                                    }
                                    .show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Invites", "Error al leer notificaciones", error.toException())
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error buscando usuario por correo", error.toException())
                }
            })
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
                var listToFiltrate = taskModel.lists
                Log.i("El ID", taskModel.currentPage.toString())
                if(taskModel.currentPage < -4){
                    listToFiltrate = taskModel.sharedLists
                }
                val list = listToFiltrate.firstOrNull { it.id == taskModel.currentPage }

                if (list != null) {
                    pageTitle.text = list.name
                } else {
                    // Manejo cuando la lista no se encuentra
                    Log.e("MainActivity", "Lista con ID ${taskModel.currentPage} no encontrada")
                    pageTitle.text = "Lista no encontrada"
                    // Opcional: resetear a una vista por defecto
                    taskModel.currentPage = -1
                    pageTitle.setText(R.string.allVista)
                }
            }
        }

        if(taskModel.currentPage < 0 && taskModel.currentPage >= -4 && toolbar.menu.hasVisibleItems()){

            for (i in 0 until toolbar.menu.size()) {
                toolbar.menu.getItem(i).isVisible = false
            }
            fab.visibility = View.GONE
            return
        }
        else if((taskModel.currentPage >= 0 || taskModel.currentPage < -4) ){
            for (i in 0 until toolbar.menu.size()) {
                val menuItem = toolbar.menu.getItem(i)
                menuItem.isVisible = true


                Log.i("El item del menu es", menuItem.title.toString())
                if(taskModel.currentPage >= 0 && menuItem.title == getString(R.string.ordenateList)){
                    menuItem.subMenu?.getItem(2)?.setVisible(false)
                    continue
                }
                else if(taskModel.currentPage < -4 && menuItem.title == getString(R.string.ordenateList)){
                    menuItem.subMenu?.getItem(2)?.setVisible(true)
                }
                else if(taskModel.currentPage < -4 && menuItem.title == getString(R.string.editList)){
                    val list = taskModel.sharedLists.firstOrNull { it.id == taskModel.currentPage }
                    Log.i("Comparación de emails", "${list?.userEmail}, ${taskModel.currentUserEmail}")
                    if(list != null && list.userEmail != taskModel.currentUserEmail){
                        menuItem.setVisible(false)
                    }
                }
                else{
                    menuItem.setVisible(true)
                }

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
        editing: Boolean,
        remoteId: String?
    ) {
        // El primer caso es que sea una tarea local, las tareas locales tienen IDS mayor a 0
        if(taskModel.currentPage > 0){
            taskModel.getListFromDb(this)
            redrawLists()
            if(!editing){
                taskModel.createTask(title, notes, importance, date, taskModel.currentPage)
                runFilters()
                return
            }
            taskModel.editTask(id, title, notes, importance,date)
            runFilters()
            return
        }

        // De lo contrario es una lista de Firebase, entonces buscamos la lista
        val list = taskModel.sharedLists.find { it.id == taskModel.currentPage }
        if(list == null){
            // Para casos en los que la lista ya no existe
            taskModel.currentPage = 0
            runFilters()
            Toast.makeText(this, "La lista ya no se encuentra disponible", Toast.LENGTH_SHORT).show()
            return
        }
        // Obtenemos la referencia de la lista interna de tasks en cada lista
        val taskListRef = taskModel.database.getReference("lists").child(list.remoteId!!).child("tasks")
        // Creamos la task que va a subirse
        val newTask = Task(-1, title, notes, importance, date, colorId = list.color )
        newTask.emailCreated = taskModel.currentUserEmail
        newTask.iconCreated = taskModel.currentUserImage
        newTask.nameCreated = navigationView.getHeaderView(0).findViewById<TextView>(R.id.nav_header_name).text.toString()
        if(editing){
            taskListRef.child(remoteId!!).setValue(newTask)
            return
        }

        taskListRef.push().setValue(newTask)

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
        editing: Boolean,
        sharedUsersList: MutableList<UserFromSharedList>
    ) {
        val lists = taskModel.database.getReference("lists")
        val editedList = TaskList(-1, title, colorId, "", icon, taskModel.currentUserId)
        editedList.userEmail = taskModel.currentUserEmail

        if (!editing) {
            val currentUserId = taskModel.currentUserId.toString()
            val currentUser = taskModel.userDao.getUserById(taskModel.currentUserId)
            val currentUserName = currentUser?.name ?: "Yo"
            val currentUserLastName = currentUser?.lastName ?: ""
            val currentUserEmail = taskModel.currentUserEmail
            val currentAvatarName = "mark" // puedes obtenerlo dinámico si lo tienes guardado
            val avatarId = R.drawable.mark

            val yaIncluido = sharedUsersList.any { it.remoteId == currentUserId }
            if (!yaIncluido) {
                sharedUsersList.add(
                    UserFromSharedList(
                        remoteId = currentUserId,
                        name = currentUserName,
                        lastName = currentUserLastName,
                        email = currentUserEmail,
                        avatar = avatarId,
                        state = "aceptado",
                        avatarName = currentAvatarName
                    )
                )
            }

            val newRef = lists.push()
            val generatedId = newRef.key ?: return

            // 🔄 Actualizar el ID remoto de la lista en caso de necesitarlo luego
            editedList.remoteId = generatedId
            newRef.setValue(editedList)

            // Guardamos sharedUsers
            val sharedUsersMap = sharedUsersList.associateBy { it.remoteId }.mapValues { (_, user) ->
                mapOf(
                    "name" to user.name,
                    "lastName" to user.lastName,
                    "email" to user.email,
                    "status" to user.state,
                    "avatarName" to user.avatarName
                )
            }

            newRef.child("sharedUsers").setValue(sharedUsersMap)
            val invitesRef = taskModel.database.getReference("pendingInvites")

            sharedUsersList
                .filter { it.state.lowercase() == "pendiente" }
                .forEach { user ->
                    val inviteData = mapOf(
                        "fromUser" to currentUserName,
                        "listName" to title,
                        "timestamp" to System.currentTimeMillis()
                    )
                    invitesRef
                        .child(user.remoteId)
                        .child(generatedId) // usamos el ID real de la nueva lista
                        .setValue(inviteData)
                }
            return
        }



        val sharedList = taskModel.sharedLists.firstOrNull { it.id == taskModel.currentPage }
        if (sharedList == null) {
            Toast.makeText(this, "La lista que se quería editar ya no existe", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedColorTask = sharedList.tasks!!.map { task ->
            task.copy(colorId = colorId)
        }
        editedList.tasks = updatedColorTask.toMutableList()

        val listRef = lists.child(id)
        listRef.setValue(editedList)

        val sharedUsersMap = sharedUsersList.associateBy { it.remoteId }.mapValues { (_, user) ->
            mapOf(
                "name" to user.name,
                "lastName" to user.lastName,
                "email" to user.email,
                "status" to user.state,
                "avatarName" to user.avatarName
            )
        }
        listRef.child("sharedUsers").setValue(sharedUsersMap)
        // 🔔 Enviar notificaciones a nuevos usuarios pendientes solo si no estaban antes
        val invitesRef = taskModel.database.getReference("pendingInvites")
        val usuariosAnteriores = sharedList.sharedUsers?.map { it.remoteId } ?: listOf()

        sharedUsersList
            .filter { it.state.lowercase() == "pendiente" && it.remoteId !in usuariosAnteriores }
            .forEach { user ->
                val currentUser = taskModel.userDao.getUserById(taskModel.currentUserId)
                val inviteData = mapOf(
                    "fromUser" to (currentUser?.name ?: "Alguien"),
                    "listName" to title,
                    "timestamp" to System.currentTimeMillis()
                )

                invitesRef
                    .child(user.remoteId)
                    .child(id)
                    .setValue(inviteData)
            }

    }


    override fun onSharedListDeleted(id: String) {
        taskModel.deleteSharedList(id)
        taskModel.currentPage = -1
        runFilters()
    }

    fun redrawLists(){
        val listsMenu = navigationView.menu.findItem(R.id.listMenuDisplay).subMenu
        listsMenu?.clear()
        for (list in taskModel.lists){
            val item = listsMenu?.add(0,list.id.hashCode(), 0, list.name)
            item?.setIcon(list.iconId)
        }
        // Esta es para poder mostrar las listas compartidas
        // Primero buscamos el submenu
        val sharedListsMenu = navigationView.menu.findItem(R.id.sharedListsMenuDisplay).subMenu
        // Le borramos todo
        sharedListsMenu?.clear()
        // Le metemos cada uno de las listas
        for(sharedList in taskModel.sharedLists){
            val item = sharedListsMenu?.add(0,sharedList.id, 0, sharedList.name)
            item?.setIcon(sharedList.iconId)
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
                taskModel.filterByList(recyclerView, taskModel.currentPage < -4)
            }
        }

        if(goDetault){
            taskModel.currentSortOrder = SortOrder.DEFAULT
            taskModel.onlyCompleted = true
        }
        if (taskModel.currentPage >= 0 || taskModel.currentPage < -4)
            runOrder(taskModel.currentSortOrder)
    }

    override fun onTaskCheckedChanged(task: Task, isChecked: Boolean) {
        if(task.id >0){
            taskModel.changeCompleted(task.id, isChecked)
        }
        else{
            taskModel.changeCompletedShared(task.remoteId!!, isChecked)
        }
        runFilters()
        if(isChecked){
            val snackbar = Snackbar.make(coordinatorLayout, R.string.completedConfirmation, Snackbar.LENGTH_LONG)
                .setAction(R.string.cancel){onTaskCheckedChanged(task, false)}
            snackbar.show()
        }
    }

    override fun OnTaskClickForEdit(task: Task) {
        Log.i("EL UD DEL TASK ES", task.remoteId.toString())
        if(taskModel.currentPage < -4 && task.emailCreated != taskModel.currentUserEmail){
            return
        }

        TaskDialogFragment.setArguments(task.id, task.title, task.notes, task.importance, task.date, task.remoteId)
            .show(supportFragmentManager, "TaskEdit")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.lista_menu, menu)
        changePageStyles()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.editList_nav->{
            if(taskModel.currentPage < -4){
                val list = taskModel.sharedLists.first { it.id == taskModel.currentPage }
                ListDialogFragment.setArguments(list.id.hashCode(), list.name, list.iconId, list.color, list.remoteId!!, list.sharedUsers).show(supportFragmentManager,"EditList")
                true
            }
            else{
                val list = taskModel.lists.first { it.id == taskModel.currentPage }
                ListDialogFragment.setArguments(list.id, list.name, list.iconId, list.color).show(supportFragmentManager,"EditList")
                true
            }
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
        R.id.order_by_user_ascending_menu ->{
            taskModel.currentSortOrder = SortOrder.USER_ASC
            runOrder(taskModel.currentSortOrder)
            true
        }
        R.id.order_by_user_descending_menu -> {
            taskModel.currentSortOrder = SortOrder.USER_DESC
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
        Log.i("Filtered", "Estoy llamando a filtered")
        when(order){
            SortOrder.IMPORTANCE_DESC->taskModel.orderByImportance(true, recyclerView)
            SortOrder.IMPORTANCE_ASC -> taskModel.orderByImportance(false, recyclerView)
            SortOrder.DATE_DESC -> taskModel.orderByDate(true, recyclerView)
            SortOrder.DATE_ASC ->  taskModel.orderByDate(false, recyclerView)
            SortOrder.USER_DESC -> taskModel.orderByUser(true, recyclerView)
            SortOrder.USER_ASC -> taskModel.orderByUser(false, recyclerView)
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
                if(task.id > 0){
                    taskModel.deleteTask(task.id, task.title, task.notes, task.importance, task.date)
                    runFilters()
                }
                else{
                    if(task.emailCreated == taskModel.currentUserEmail) {
                        taskModel.deleteSharedTask(task.remoteId!!)
                    }

                }

                true
            }
            //Aqui para cambiar fecha de vencimiento
            R.id.changeDateMenu ->{
                if(taskModel.currentPage < -4 && task.emailCreated != taskModel.currentUserEmail) {
                    return true
                }

                val dateDialog = DateDialogFragment()
                dateDialog.show(supportFragmentManager, "datePicker")
                true
            }
            R.id.removeImportanceMenu ->{ //Si eligieron eliminar importancia, llamamos a la función
                if(taskModel.currentPage >= -4){
                    taskModel.editTask(task.id, task.title, task.notes, 0, task.date)
                }
                else{
                    if(task.emailCreated == taskModel.currentUserEmail) {
                        taskModel.removeImportanceForShared(task.remoteId!!)
                    }
                }
                runFilters()
                true
            }
            R.id.setTodayLimitMenu->{ // Settea la fecha de hoy
                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = formatter.format(System.currentTimeMillis())
                if(taskModel.currentPage >= -4){
                    taskModel.changeDateLimit(task.id, today)
                }
                else{
                    if(task.emailCreated == taskModel.currentUserEmail) {
                        taskModel.changeDataLimitForShared(task.remoteId!!, today)
                    }
                }
                runFilters()
                true
            }
            R.id.setTomorrowLimitMenu->{ // Settea la fecha de mañana
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1) // Suma un día
                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) // Formato de fecha
                val tomorrow = formatter.format(calendar.time)
                if(taskModel.currentPage >= -4){
                    taskModel.changeDateLimit(task.id, tomorrow)
                }
                else{
                    if(task.emailCreated == taskModel.currentUserEmail) {
                        taskModel.changeDataLimitForShared(task.remoteId!!, tomorrow)
                    }

                }
                runFilters()
                true
            }
            R.id.removeDateMenu->{ // Remueve la fecha
                if(taskModel.currentPage >= -4){
                    taskModel.editTask(task.id, task.title, task.notes, task.importance, null)
                }
                else{
                    if(task.emailCreated == taskModel.currentUserEmail) {
                        taskModel.changeDataLimitForShared(task.remoteId!!, "")
                    }
                }
                runFilters()
                true
            }
            R.id.completeActionMenu->{ // Completa la tarea
                if(taskModel.currentPage >= -4){
                    taskModel.changeCompleted(task.id, true)
                }
                else{
                    taskModel.changeCompletedShared(task.remoteId!!, true)
                }
                runFilters()
                true
            }
            else->{
                false
            }
        }
    }

    fun abrirListaCompartida(list: TaskList) {
        taskModel.currentPage = list.id
        taskModel.filterByList(recyclerView, shared = true)
        runFilters()
        redrawLists()
        changePageStyles()
    }
    fun onSharedListClicked(list: TaskList) {
        val currentUserEmail = taskModel.currentUserEmail
        val currentUserInList = list.sharedUsers?.firstOrNull { it.email == currentUserEmail }
        Log.d("user", "" + list.sharedUsers)
        Log.d("email", "" + currentUserEmail + " " + currentUserInList)
        if (currentUserInList != null && currentUserInList.state == "pendiente") {
            AlertDialog.Builder(this)
                .setTitle("Invitación pendiente")
                .setMessage("No has aceptado la invitación a esta lista.\n¿Deseas aceptarla?\nSi no aceptas, se rechazará y se eliminará de tus listas.")
                .setPositiveButton("Aceptar") { _, _ ->
                    val userRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("lists")
                        .child(list.remoteId!!)
                        .child("sharedUsers")
                        .child(currentUserInList.remoteId)
                        .child("status")

                    userRef.setValue("aceptado").addOnSuccessListener {
                        Toast.makeText(this, "¡Invitación aceptada!", Toast.LENGTH_SHORT).show()
                        abrirListaCompartida(list)
                    }
                }
                .setNegativeButton("Rechazar") { _, _ ->
                    val userRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("lists")
                        .child(list.remoteId!!)
                        .child("sharedUsers")
                        .child(currentUserInList.remoteId)

                    userRef.updateChildren(mapOf(
                        "status" to "rechazado"
                    )).addOnSuccessListener {
                        Toast.makeText(this, "Invitación rechazada", Toast.LENGTH_SHORT).show()
                        taskModel.sharedLists.remove(list)
                        redrawLists()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error al rechazar la invitación", Toast.LENGTH_SHORT).show()
                    }
                }
                .setCancelable(false)
                .show()
        } else {
            abrirListaCompartida(list)
        }
    }

    override fun onDateSelected(year: Int, month: Int, day: Int) {
        // Parceamos los datos
        val finalDay = if(day < 10) "0${day}" else day
        val finalMonth = if(month < 10) "0${month}" else month
        val finalDate = "${year}-${finalMonth}-${finalDay}"
        Log.i("Fecha final", finalDate)
        val task = taskModel.taskAdapter.itemList[taskModel.itemPosition]

        if(taskModel.currentPage > -4){
            taskModel.changeDateLimit(task.id, finalDate)
        }
        else{
            taskModel.changeDataLimitForShared(task.remoteId!!, finalDate)
        }
        runFilters()
    }
}