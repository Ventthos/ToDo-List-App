package com.ventthos.todo_list_app

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.DialogFragment

interface InvitationDialogListener {
    fun onAcceptInvitation()
    fun onRejectInvitation()
}

class InvitationDialogFragment : DialogFragment() {

    private var listener: InvitationDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is InvitationDialogListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement InvitationDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Invitación pendiente")
            .setMessage("No has aceptado la invitación a esta lista.\n¿Deseas aceptarla?\nSi no aceptas, se rechazará y se eliminará de tus listas.")
            .setPositiveButton("Aceptar") { _, _ -> listener?.onAcceptInvitation() }
            .setNegativeButton("Rechazar") { _, _ -> listener?.onRejectInvitation() }
            .create()

        // Bloquear cierre al tocar fuera del diálogo
        dialog.setCanceledOnTouchOutside(false)

        // Interceptar botón de retroceso
        dialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }

        return dialog
    }


    companion object {
        fun newInstance(): InvitationDialogFragment {
            return InvitationDialogFragment()
        }
    }
}
