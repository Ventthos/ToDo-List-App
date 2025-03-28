package com.ventthos.todo_list_app

class ColorObject (val colorId: Int, var name: String, hex:String, hexBackground: String, cotrastHex:String){
    val id: Int = colorId
    val hexHash: String = "#$hex"
    val hexBackgroundHash: String = "#$hexBackground"
    val hexContrastHash: String = "#$cotrastHex"
}