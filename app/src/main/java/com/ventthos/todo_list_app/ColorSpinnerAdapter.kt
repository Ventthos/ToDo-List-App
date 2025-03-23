package com.ventthos.todo_list_app
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ColorSpinnerAdapter(context: Context, list : List<ColorObject>)
    : ArrayAdapter<ColorObject>(context, 0 , list)
{
    private var layoutInflater = LayoutInflater.from(context)

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val view: View = layoutInflater.inflate(R.layout.color_spinner_bg, null, true)
        return view(view, position)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        var cv = convertView
        if(cv == null)
            cv = layoutInflater.inflate(R.layout.color_spinner_item, parent, false)
        return view(cv!!, position)
    }

    private fun view(view: View, position: Int): View
    {
        val colorObject : ColorObject = getItem(position) ?: return view

        val colorName = view.findViewById<TextView>(R.id.colorName)
        val colorBlob = view.findViewById<View>(R.id.colorBlob)
        val colorBlobBg = view.findViewById<View>(R.id.colorBlobBackground)
        val colorNameBG = view.findViewById<TextView>(R.id.colorNameBg)

        colorNameBG?.text = colorObject.name
        colorNameBG?.setTextColor(Color.parseColor(colorObject.hexContrastHash))

        colorName?.text = colorObject.name

        colorBlob?.background?.setTint(Color.parseColor(colorObject.hexHash))
        colorBlobBg?.background?.setTint(Color.parseColor(colorObject.hexBackgroundHash))

        return view
    }
}