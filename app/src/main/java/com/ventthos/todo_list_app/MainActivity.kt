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
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

class ListDialogFragment : DialogFragment() {

    lateinit var spinner: Spinner
    /*
    interface DatePickerListener {
        fun onDateSelected(year: Int, month: Int, day: Int)
    }

    private var listener: DatePickerListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? DatePickerListener
    }
    */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val dialogView = inflater.inflate(R.layout.list_window, null)

            spinner = dialogView.findViewById(R.id.colorSpinner)

            builder.setView(dialogView)
            loadColorSpinner()

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun loadColorSpinner(){
        var selectedColor = ColorList().defaulColor
        spinner.apply {
            adapter = ColorSpinnerAdapter(context, ColorList().basicColors())
            setSelection(ColorList().colorPosition(selectedColor), false)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long)
                {
                    selectedColor = ColorList().basicColors()[position]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
    }
}

//Clase para los items dentro de el recicler view
data class Item(val title: String)

//Adaptador para la clase Item
class ItemAdapter(private val itemList: List<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.item_title)
        //val description: TextView = view.findViewById(R.id.item_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.title.text = item.title
        //holder.description.text = item.description
    }

    override fun getItemCount() = itemList.size
}
class MainActivity : AppCompatActivity() {
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var drawerToggle: ActionBarDrawerToggle
    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Logica del reciclerView
        val recyclerView = findViewById<RecyclerView>(R.id.reciclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        //Aqui jalariamos los elementos de la base de datos para meterlos a una lista
        val items = listOf(
            Item("Elemento 1"),
            Item("Elemento 2"),
            Item("Elemento 3")
        )

        recyclerView.adapter = ItemAdapter(items)
        //termina logia del recicler view

        window.statusBarColor = ContextCompat.getColor(this, R.color.mainColor)

        // find views
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)

        // Drawer configuration
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawerDesc, R.string.closeDrawerDesc)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navAddList->{
                    ListDialogFragment().show(supportFragmentManager, "List")
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


    }
}