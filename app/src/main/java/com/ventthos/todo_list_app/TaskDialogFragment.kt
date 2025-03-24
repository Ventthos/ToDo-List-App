package com.ventthos.todo_list_app

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment

class TaskDialogFragment: DialogFragment()  {

    private lateinit var titleInput: EditText
    private lateinit var notesInput: EditText
    private lateinit var importanceSelector: Spinner
    private lateinit var dateSelector: EditText

    private val IDTAG = "TaskId"
    private val TITLETAG = "TitleSelected"
    private val NOTESTAG = "NotesSelected"
    private val IMPORTANCETAG = "importanceSelected"
    private val DATETAG = "DateSelected"
    private val EDITINGTAG = "TaskEditState"

    private var id = -1
    private var editing = false

    interface TaskEditListener {
        fun onTaskEdit(id: Int, title: String,notes: String, importance: Int, date: String, editing: Boolean)
    }

    private var listener: TaskEditListener? = null

    private fun sendValues(){
        val title = titleInput.text.toString()
        val notes = notesInput.text.toString()
        val importance = importanceSelector.selectedItem as? Int ?: 0
        val date = dateSelector.text.toString()
        listener?.onTaskEdit(id, title, notes,importance,date,editing)

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
            date: String? = null
        ): TaskDialogFragment {
            val fragment = TaskDialogFragment()
            val args = Bundle()

            args.putInt("ID", id)
            args.putString("TITLE", title)
            args.putString("NOTES", notes)
            args.putInt("IMPORTANCE", importance)
            args.putString("DATE", date ?: "")

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

            // Spinner config
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arrayOf(0,1,2,3,4))
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            importanceSelector.adapter = adapter

            if(savedInstanceState != null){
                titleInput.setText(savedInstanceState.getString(TITLETAG, ""))
                notesInput.setText(savedInstanceState.getString(NOTESTAG, ""))
                importanceSelector.setSelection(savedInstanceState.getInt(IMPORTANCETAG, 0))
                dateSelector.setText(savedInstanceState.getString(DATETAG, ""))
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
                }
            }

            builder.setView(dialogView)
                .setPositiveButton(R.string.save){_,_->
                    sendValues()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(IDTAG, id)
        outState.putString(TITLETAG, titleInput.text.toString())
        outState.putString(NOTESTAG, notesInput.text.toString())
        outState.putInt(IMPORTANCETAG, importanceSelector.selectedItem as? Int ?: 0)
        outState.putString(DATETAG, dateSelector.text.toString())
        outState.putBoolean(EDITINGTAG, editing)
    }
}