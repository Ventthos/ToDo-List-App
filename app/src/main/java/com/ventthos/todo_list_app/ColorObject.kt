package com.ventthos.todo_list_app

class ColorObject (var name: String, var hex:String, hexBackground: String, cotrastHex:String){
    val hexHash: String = "#$hex"
    val hexBackgroundHash: String = "#$hexBackground"
    val hexContrastHash: String = "#$cotrastHex"
}