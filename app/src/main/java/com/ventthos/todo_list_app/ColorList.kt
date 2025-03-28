package com.ventthos.todo_list_app

import android.graphics.Color

val blackHex = "000000"
var whiteHex = "FFFFFF"

var basicColors = listOf(
    ColorObject(0, "Rojo", "be2525", "FBD6D6", whiteHex),
    ColorObject(1, "Naranja", "e47421", "fbe5c8", whiteHex),
    ColorObject(2, "Amarillo", "f9ad4d", "f8fbc8", whiteHex),
    ColorObject(3, "Verde", "14bd02", "d4fbc8", whiteHex),
    ColorObject(4, "Azul", "18578a", "C8E2FA", whiteHex),
    ColorObject(5, "Morado", "8702bd", "dccff7", whiteHex),
    ColorObject(6, "Rosa", "ea1f63", "fae4ec", whiteHex)
)

class ColorList {


    val defaulColor: ColorObject = basicColors()[0]

    fun colorPosition(colorObject: ColorObject): Int{
        for(i in basicColors().indices){
            if(colorObject === basicColors()[i]){
                return i
            }
        }
        return 0
    }

    fun basicColors(): List<ColorObject>{
        return basicColors
    }
}