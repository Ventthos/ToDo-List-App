package com.ventthos.todo_list_app

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

val iconsList = mutableListOf(
    R.drawable.consola,
    R.drawable.carro,
    R.drawable.compras,
    R.drawable.corazon,
    R.drawable.dinero,
    R.drawable.graduacion,
    R.drawable.pelota,
    R.drawable.personas,
    R.drawable.taza_de_cafe,
    R.drawable.pesas
)

class IconPicker: DialogFragment(){
    private lateinit var iconsAdapter: IconsAdapter


    interface IconPickerListener {
        fun onIconSelected(id:Int)
    }

    private var listener: IconPickerListener? = null

    fun setListener(listener: IconPickerListener) {
        this.listener = listener
    }

    private lateinit var recyclerView: RecyclerView
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;

            val dialogView = inflater.inflate(R.layout.icon_selector, null)

            iconsAdapter = IconsAdapter(iconsList, ::selectedIcon,this)
            recyclerView = dialogView.findViewById(R.id.iconsRecyclerView)

            //val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 2
            val spanCount = 4
            recyclerView.layoutManager = GridLayoutManager(requireActivity(), spanCount)
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = iconsAdapter

            builder.setView(dialogView)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun selectedIcon(id: Int){
        Log.i("muajajaja2", "Si")
        listener?.onIconSelected(id)
        dialog?.dismiss()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}