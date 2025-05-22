package com.ventthos.todo_list_app

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class TaskDialogFragment: DialogFragment(), DateDialogFragment.DatePickerListener  {

    private lateinit var titleInput: EditText
    private lateinit var notesInput: EditText
    private lateinit var importanceSelector: Spinner
    private lateinit var dateSelector: EditText
    private lateinit var windowTitle: TextView

    private val IDTAG = "TaskId"
    private val TITLETAG = "TitleSelected"
    private val NOTESTAG = "NotesSelected"
    private val IMPORTANCETAG = "importanceSelected"
    private val DATETAG = "DateSelected"
    private val EDITINGTAG = "TaskEditState"
    private val REMOTEIDTAG = "RemoteId"

    private var id = -1
    private var editing = false
    private var remoteId = ""

    interface TaskEditListener {
        fun onTaskEdit(id: Int, title: String,notes: String, importance: Int, date: String, editing: Boolean, remoteId: String?)
    }

    private var listener: TaskEditListener? = null

    private fun sendValues(){
        val title = titleInput.text.toString()
        val notes = notesInput.text.toString()
        val importance = importanceSelector.selectedItemPosition
        val date = dateSelector.text.toString()
        listener?.onTaskEdit(id, title, notes,importance,date,editing, remoteId)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? TaskEditListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        fun setArguments(
            id: Int = -1,
            title: String = "",
            notes: String = "",
            importance: Int = 0,
            date: String? = null,
            remoteId: String? = ""
        ): TaskDialogFragment {
            val fragment = TaskDialogFragment()
            val args = Bundle()

            args.putInt("ID", id)
            args.putString("TITLE", title)
            args.putString("NOTES", notes)
            args.putInt("IMPORTANCE", importance)
            args.putString("DATE", date ?: "")
            args.putString("REMOTEID", remoteId)

            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val dialogView = inflater.inflate(R.layout.task_window, null)

            // FindViews
            titleInput = dialogView.findViewById(R.id.titleInput)
            notesInput = dialogView.findViewById(R.id.notesInput)
            importanceSelector = dialogView.findViewById(R.id.importanceSpinner)
            dateSelector = dialogView.findViewById(R.id.dateSelector)
            windowTitle = dialogView.findViewById(R.id.taskWindowTitle)

            // binds
            dateSelector.setOnClickListener {
                val datePicker = DateDialogFragment()
                datePicker.show(childFragmentManager, "DatePicker")
            }

            // Spinner config
            val importance = resources.getStringArray(R.array.importanceArrayOptions)
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,  importance)

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            importanceSelector.adapter = adapter

            if(savedInstanceState != null){
                titleInput.setText(savedInstanceState.getString(TITLETAG, ""))
                notesInput.setText(savedInstanceState.getString(NOTESTAG, ""))
                importanceSelector.setSelection(savedInstanceState.getInt(IMPORTANCETAG, 0))
                dateSelector.setText(savedInstanceState.getString(DATETAG, ""))
                editing = savedInstanceState.getBoolean(EDITINGTAG)
                remoteId = savedInstanceState.getString(REMOTEIDTAG, "")

            }
            else {
                // Cargar valores iniciales desde argumentos
                if (arguments != null) {
                    id = arguments?.getInt("ID", -1)?:-1
                    titleInput.setText(arguments?.getString("TITLE", "") ?: "")
                    notesInput.setText(arguments?.getString("NOTES", "") ?: "")
                    importanceSelector.setSelection(arguments?.getInt("IMPORTANCE", 0) ?: 0)
                    dateSelector.setText(arguments?.getString("DATE", "") ?: "")
                    editing = true
                    remoteId = arguments?.getString("REMOTEID", "")?: ""
                    Log.i("ME HAN PROPORCIONADO", remoteId)
                }
            }

            if(editing)
                windowTitle.setText(R.string.editTask)

            builder.setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel){_,_->
                    dialog?.dismiss()
                }

            val dialog = builder.create()

            // Esto es necesario para poder hacer de que si la validación falla, se envie un error
            dialog.setOnShowListener {
                // Botón Guardar
                val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                saveButton.setOnClickListener {
                    if (validateInput()) {
                        sendValues()
                        dialog.dismiss()
                    }
                }
            }

            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    // Función para saber que la lista tiene un título
    private fun validateInput(): Boolean {
        val title = titleInput.text.toString()
        return if (title.isBlank()) {
            Toast.makeText(context, R.string.taskTitleError, Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(IDTAG, id)
        outState.putString(TITLETAG, titleInput.text.toString())
        outState.putString(NOTESTAG, notesInput.text.toString())

        outState.putInt(IMPORTANCETAG, importanceSelector.selectedItemPosition)
        outState.putString(DATETAG, dateSelector.text.toString())
        outState.putBoolean(EDITINGTAG, editing)

    }

    override fun onDateSelected(year: Int, month: Int, day: Int) {
        val finalDay = if(day < 10) "0${day}" else day
        val finalMonth = if(month < 10) "0${month}" else month
        dateSelector.setText("${year}-${finalMonth}-${finalDay}")
    }
}