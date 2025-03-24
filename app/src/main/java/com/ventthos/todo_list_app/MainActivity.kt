package com.ventthos.todo_list_app

import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView


//Clase para los items dentro de el recicler view
data class Task(val id: Int, var title: String, var notes: String = "", var importance: Int, var date: String?, var completed: Boolean = false)

//Adaptador para la clase Item
class ItemAdapter(private val itemList: List<Task>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    lateinit var inflater: LayoutInflater

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.task_title)
        val date: TextView = view.findViewById(R.id.taskDate)
        val completed: CheckBox = view.findViewById(R.id.taskCompleted)
        val starsContainer: LinearLayout = view.findViewById(R.id.starsContainer)
        val dateContainer: CardView = view.findViewById(R.id.datContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.title.text = item.title
        holder.completed.isChecked = item.completed

        if(item.date != null){
            holder.date.text = "Límite ${item.date}"
        }
        else{
            holder.dateContainer.visibility = View.GONE
        }

        holder.starsContainer.removeAllViews()
        for (i in 0 until  item.importance){
            val starView = inflater.inflate(R.layout.star, holder.starsContainer, false)
            holder.starsContainer.addView(starView)
        }
    }

    override fun getItemCount() = itemList.size
}


class MainActivity : AppCompatActivity(), TaskDialogFragment.TaskEditListener {
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var drawerToggle: ActionBarDrawerToggle
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var fab: FloatingActionButton

    private val taskModel: TaskModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Logica del reciclerView
        val recyclerView = findViewById<RecyclerView>(R.id.reciclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        recyclerView.adapter = taskModel.taskAdapter
        //termina logia del recicler view

        window.statusBarColor = ContextCompat.getColor(this, R.color.mainColor)

        // find views
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)
        fab = findViewById(R.id.fab)

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
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        fab.setOnClickListener{
            TaskDialogFragment().show(supportFragmentManager, "Task")
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
            taskModel.createTask(title, notes, importance, date)
        }
    }


}