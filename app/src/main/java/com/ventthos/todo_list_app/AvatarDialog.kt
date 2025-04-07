package com.ventthos.todo_list_app

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView

class AvatarDialog(
    private val context: Context,
    private val onAvatarSelected: (Int) -> Unit
) {
    private val avatarList = listOf(
        R.drawable.xmen,
        R.drawable.wolverine,
        R.drawable.vision,
        R.drawable.spiderman,
        R.drawable.spawn,
        R.drawable.ironman,
        R.drawable.deadpool,
        R.drawable.daredevil,
        R.drawable.cyclops,
        R.drawable.capitanamerica,
        R.drawable.blackpanter,
        R.drawable.hierro
    )

    fun show() {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_avatar_picker)

        val gridView = dialog.findViewById<GridView>(R.id.avatarGrid)
        gridView.adapter = object : BaseAdapter() {
            override fun getCount(): Int = avatarList.size
            override fun getItem(position: Int): Any = avatarList[position]
            override fun getItemId(position: Int): Long = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val imageView = ImageView(context)
                imageView.layoutParams = AbsListView.LayoutParams(200, 200)
                imageView.setImageResource(avatarList[position])
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setBackgroundResource(R.drawable.circle_background)
                return imageView
            }
        }

        gridView.setOnItemClickListener { _, _, position, _ ->
            onAvatarSelected(avatarList[position])
            dialog.dismiss()
        }

        dialog.show()
    }
}