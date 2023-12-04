package com.capstone.envibinconnect.Others

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import com.capstone.envibinconnect.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class GiveAlert(context : Context) {
    private lateinit var context: Context
    init {
        this.context = context
    }

    fun sendAlertDialog(title : String, message : String){
        MaterialAlertDialogBuilder(this.context)
            .setCancelable(true)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(R.drawable.baseline_person_24)
            .setCancelable(true)
            .setNegativeButton("Cancel", null)
            .setBackground(this.context.resources?.getDrawable(R.drawable.alert_dialog))
            .show()
    }

    fun sendAlertDialog(title : String, message : String, icon : Int){
        MaterialAlertDialogBuilder(this.context)
            .setCancelable(true)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(icon)
            .setCancelable(true)
            .setNegativeButton("Cancel", null)
            .setBackground(this.context.resources?.getDrawable(R.drawable.alert_dialog))
            .show()
    }

    fun sendSnackbar(message : String, view : View){
        Snackbar.make(
            view,
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }
}