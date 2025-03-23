package com.ventthos.todo_list_app

import android.graphics.Color

class ColorList {
    private val blackHex = "000000"
    private var whiteHex = "FFFFFF"

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
        return listOf(
            ColorObject("Rojo", "be2525", "FBD6D6", whiteHex),
            ColorObject("Naranja", "e47421", "fbe5c8", whiteHex),
            ColorObject("Amarillo", "efea00", "f8fbc8", whiteHex),
            ColorObject("Verde", "14bd02", "d4fbc8", whiteHex),
            ColorObject("Azul", "18578a", "C8E2FA", whiteHex),
            ColorObject("Morado", "8702bd", "dccff7", whiteHex)

        )
    }
}