package com.ventthos.todo_list_app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.ventthos.todo_list_app.db.AppDatabase.AppDatabase
import com.ventthos.todo_list_app.db.dataclasses.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var lastnameInput: EditText
    private lateinit var newPasswordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        emailInput = findViewById(R.id.email_input)
        nameInput = findViewById(R.id.name_input)
        lastnameInput = findViewById(R.id.lastname_input)
        newPasswordInput = findViewById(R.id.new_password_input)
        confirmPasswordInput = findViewById(R.id.confirm_password_input)
        resetButton = findViewById(R.id.reset_password_button)

        val db = AppDatabase.getDatabase(this)
        val userDao = db.UserDao()

        resetButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val name = nameInput.text.toString().trim()
            val lastName = lastnameInput.text.toString().trim()
            val newPassword = newPasswordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (email.isEmpty() || name.isEmpty() || lastName.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val user = userDao.getUserByEmail(email)

                if (user == null) {
                    runOnUiThread {
                        Toast.makeText(this@ForgotPasswordActivity, "El correo no está registrado", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                if (user.name != name || user.lastName != lastName) {
                    runOnUiThread {
                        Toast.makeText(this@ForgotPasswordActivity, "Nombre o apellido incorrectos", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val updatedUser = user.copy(password = newPassword)
                userDao.updateUser(updatedUser)

                runOnUiThread {
                    Toast.makeText(this@ForgotPasswordActivity, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this@ForgotPasswordActivity, LoginActivity::class.java))
                        finish()
                    }, 2000)
                }

            }
        }
    }
}