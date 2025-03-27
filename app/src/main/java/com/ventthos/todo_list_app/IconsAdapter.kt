package com.ventthos.todo_list_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView



class IconsAdapter(private var icons: MutableList<Int>, val onClickFunction: (Int) -> Unit, val activity: DialogFragment): RecyclerView.Adapter<IconsAdapter.ViewHolder>(){
    class ViewHolder(val view: View, val onClickFunction: (Int) -> Unit): RecyclerView.ViewHolder(view){

        private var iconButton: ImageButton
        private var buttonBackground: CardView
        private var icon: Int = -45

        init {
            iconButton = view.findViewById(R.id.iconButton)
            buttonBackground = view.findViewById(R.id.buttonBackground)
        }

        fun bind(icon: Int){
            this.icon = icon


            iconButton.setImageResource(this.icon)
            iconButton.setOnClickListener {
                onClickFunction(this.icon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.icon_selector_item, parent, false)

        // Para el context menu
        //activity.registerForContextMenu(view)

        val vh = ViewHolder(view, onClickFunction)
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)= holder.bind(icons[position])

    override fun getItemCount() = icons.size
}