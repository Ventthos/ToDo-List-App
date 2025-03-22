package com.ventthos.todo_list_app

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var buttonPrueba: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        buttonPrueba = findViewById(R.id.buttonPrueba)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        buttonPrueba.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
    }
}