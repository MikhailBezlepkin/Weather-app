package com.example.weatherapp

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

object DialogManager {
    fun locationSettingsDialog(context:Context, listener:Listener){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle("Включить геолокацию?")
        dialog.setMessage("При выключенной выключенной геолокации запрос по местоположению не возможен")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "ДА"){_,_->
            listener.onClick()
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE,"НЕТ"){_,_->
            dialog.dismiss()
        }
        dialog.show()
    }
    interface Listener{
        fun onClick()
    }
}