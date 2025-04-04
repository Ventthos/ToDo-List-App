package com.ventthos.todo_list_app

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.io.Console

class ListDialogFragment : DialogFragment(), IconPicker.IconPickerListener{
    lateinit var titleInput: EditText
    lateinit var iconChangerButton: ImageButton
    lateinit var spinner: Spinner

    private var id = -1
    private var editing = false
    private var currentIcon = -1

    private val IDTAG = "TaskId"
    private val TITLETAG = "TitleSelected"
    private val ICONTAG = "IconSelected"
    private val EDITINGTAG = "TaskEditState"

    companion object {
        fun setArguments(
            id: Int = -1,
            title: String = "",
            currentIcon: Int,
            colorId: Int
        ): ListDialogFragment {
            val fragment = ListDialogFragment()
            val args = Bundle()

            args.putInt("ID", id)
            args.putString("TITLE", title)
            args.putInt("ICON", currentIcon)
            args.putInt("COLORID", colorId)


            fragment.arguments = args
            return fragment
        }
    }


    interface ListEditorListener {
        fun onListEdited(id: Int, title: String, icon: Int, colorId: Int, editing: Boolean)
        fun onListDeleted(id: Int, title: String, icon: Int, colorId: Int, editing: Boolean)
    }

    private var listener: ListEditorListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? ListEditorListener
    }

    private fun sendValues(deleting: Boolean){
        val title = titleInput.text.toString()
        val color = spinner.selectedItem as? ColorObject

        if(!deleting && (currentIcon == 0 || title == "")){
            Toast.makeText(this.context, R.string.fillingError, Toast.LENGTH_SHORT).show()
            return
        }

        if(deleting){
            listener?.onListDeleted(id, title, currentIcon, color?.colorId?: basicColors.first().colorId, editing)
            return
        }

        listener?.onListEdited(id, title, currentIcon, color?.colorId?: basicColors.first().colorId, editing)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val dialogView = inflater.inflate(R.layout.list_window, null)

            titleInput = dialogView.findViewById(R.id.titleInput)
            spinner = dialogView.findViewById(R.id.colorSpinner)
            iconChangerButton = dialogView.findViewById(R.id.iconChangerButton)

            // Bindings
            iconChangerButton.setOnClickListener {
                val iconPicker = IconPicker()
                iconPicker.setTargetFragment(this, 0)
                iconPicker.show(parentFragmentManager, "IconPicker")
            }

            loadColorSpinner()

            if(savedInstanceState != null){
                titleInput.setText(savedInstanceState.getString(TITLETAG, ""))
                id = savedInstanceState.getInt(IDTAG)
                onIconSelected(savedInstanceState.getInt(ICONTAG))
                editing = savedInstanceState.getBoolean(EDITINGTAG)
            }
            else {
                // Cargar valores iniciales desde argumentos
                if (arguments != null) {
                    id = arguments?.getInt("ID", -1)?:-1
                    titleInput.setText(arguments?.getString("TITLE", "") ?: "")
                    onIconSelected(arguments?.getInt("ICON", -1) ?: -1)

                    val colorSelected = arguments?.getInt("COLORID", -1) ?: 0
                    spinner.setSelection(colorSelected)

                    editing = true
                }
            }


            builder.setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }

            if (editing) {
                builder.setNeutralButton(R.string.eliminar){_,_ ->sendValues(true)}
            }

            val dialog = builder.create()

            dialog.setOnShowListener {
                // Botón Guardar
                val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                saveButton.setOnClickListener {
                    if (validateInput()) {
                        sendValues(false)
                        dialog.dismiss()
                    }
                }
            }

            dialog

        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun validateInput(): Boolean {
        val title = titleInput.text.toString()
        return if (currentIcon == -1 || title.isBlank()) {
            Toast.makeText(context, R.string.fillingError, Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
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

    override fun onIconSelected(id: Int){
        if(id == -1)
            return

        iconChangerButton.setImageResource(id)
        currentIcon = id

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(IDTAG, id)
        outState.putString(TITLETAG, titleInput.text.toString())
        outState.putInt(ICONTAG, currentIcon)
        outState.putBoolean(EDITINGTAG, editing)
    }
}