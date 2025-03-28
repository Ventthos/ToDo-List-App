package com.ventthos.todo_list_app

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.ventthos.todo_list_app.IconPicker.IconPickerListener

class DateDialogFragment : DialogFragment() {
    interface DatePickerListener {
        fun onDateSelected(year: Int, month: Int, day: Int)
    }

    private var listener: DatePickerListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = targetFragment as? DatePickerListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;

            val dialogView = inflater.inflate(R.layout.date_picker, null)
            val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)


            builder.setView(dialogView)
                .setPositiveButton(R.string.saveDate){ _, _ ->
                    val year = datePicker.year
                    val month = datePicker.month+1
                    val day = datePicker.dayOfMonth
                    listener?.onDateSelected(year, month, day)
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
