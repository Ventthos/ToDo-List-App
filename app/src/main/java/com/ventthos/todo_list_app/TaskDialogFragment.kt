package com.ventthos.todo_list_app

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Spinner
import androidx.fragment.app.DialogFragment

class TaskDialogFragment: DialogFragment()  {

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
            val dialogView = inflater.inflate(R.layout.task_window, null)

            builder.setView(dialogView)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}