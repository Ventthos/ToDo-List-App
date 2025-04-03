package com.ventthos.todo_list_app

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.ventthos.todo_list_app.db.dataclasses.Task

//Adaptador para la clase Item
class ItemAdapter(
    val itemList: MutableList<Task>,
    private val listener: OnTaskCheckedChangeListener,
    private val listenerClick: OnTaskClickForEditListener,
    val activity: AppCompatActivity
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    lateinit var inflater: LayoutInflater

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.task_title)
        val date: TextView = view.findViewById(R.id.taskDate)
        val completed: CheckBox = view.findViewById(R.id.taskCompleted)
        val starsContainer: LinearLayout = view.findViewById(R.id.starsContainer)
        val dateContainer: CardView = view.findViewById(R.id.datContainer)
        val infoContainer: LinearLayout = view.findViewById(R.id.infoContainer)
        val checkBoxContainer: LinearLayout = view.findViewById(R.id.checkContainer)
        val cardRoot: CardView = view.findViewById(R.id.taskRoot)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        inflater = LayoutInflater.from(parent.context)


        activity.registerForContextMenu(view)

        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.title.text = item.title

        holder.completed.setOnCheckedChangeListener(null)
        holder.completed.isChecked = item.completed


        if(item.date != null){
            holder.date.text = "Límite ${item.date}"
            holder.dateContainer.visibility = View.VISIBLE
        }
        else{
            holder.dateContainer.visibility = View.GONE
        }


        holder.starsContainer.removeAllViews()
        for (i in 0 until  item.importance){
            val starView = inflater.inflate(R.layout.star, holder.starsContainer, false)
            holder.starsContainer.addView(starView)
        }

        holder.completed.setOnCheckedChangeListener { _, isChecked ->
            listener.onTaskCheckedChanged(item, isChecked)
        }

        // Color settings
        val color = basicColors[item.colorId]
        val mainColor = Color.parseColor(color.hexHash)
        holder.infoContainer.setBackgroundColor(Color.parseColor(color.hexBackgroundHash))
        holder.checkBoxContainer.setBackgroundColor(mainColor)
        holder.dateContainer.setCardBackgroundColor(mainColor)

        // Edit
        holder.cardRoot.setOnClickListener{
            listenerClick.OnTaskClickForEdit(item)
        }

        // Context menu
        holder.cardRoot.setOnLongClickListener {
            it.showContextMenu()
            true
        }
    }

    override fun getItemCount() = itemList.size

    fun updateList(newList: List<Task>, recyclerView: RecyclerView) {
        itemList.clear()
        itemList.addAll(newList)
        recyclerView.post {
            notifyDataSetChanged()
        }
    }
}