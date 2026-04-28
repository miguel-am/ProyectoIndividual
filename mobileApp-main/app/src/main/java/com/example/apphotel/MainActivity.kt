package com.example.apphotel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.apphotel.ui.theme.AppHotelTheme
import com.example.examenprueba1.view.scaffold.MyApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppHotelTheme (darkTheme = false){
                MyApp()
            }
        }
    }
}

