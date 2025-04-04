package com.ventthos.todo_list_app

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.ventthos.todo_list_app.db.AppDatabase.AppDatabase
import com.ventthos.todo_list_app.db.dataclasses.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Referencias a los campos
        nameInput = findViewById(R.id.register_name)
        lastNameInput = findViewById(R.id.register_lastname)
        emailInput = findViewById(R.id.register_email)
        passwordInput = findViewById(R.id.register_password)
        confirmPasswordInput = findViewById(R.id.register_confirm_password)
        registerButton = findViewById(R.id.button_register)

        val db = AppDatabase.getDatabase(this)
        val userDao = db.UserDao()

        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            // Validación de campos
            if (name.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val existingUser = userDao.getUserByEmail(email)

                if (existingUser != null) {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Ese correo ya está registrado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Crear nuevo usuario con avatar por defecto (puedes cambiar el número por una imagen real si quieres)
                    val newUser = User(
                        id = 0,
                        name = name,
                        lastName = lastName,
                        email = email,
                        password = password,
                        avatar = R.drawable.ic_launcher_foreground
                    )

                    userDao.insertUser(newUser)

                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "¡Usuario registrado con éxito!", Toast.LENGTH_SHORT).show()
                        // Volver al Login
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}