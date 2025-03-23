package com.ventthos.todo_list_app

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.fragment.app.DialogFragment

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