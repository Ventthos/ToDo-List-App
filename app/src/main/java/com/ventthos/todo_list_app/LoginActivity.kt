package com.ventthos.todo_list_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ventthos.todo_list_app.db.AppDatabase.AppDatabase
import com.ventthos.todo_list_app.db.dataclasses.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var forgotPasswordText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.get_email_login)
        passwordInput = findViewById(R.id.get_pass_login)
        loginButton = findViewById(R.id.Login_button)
        registerButton = findViewById(R.id.signup_button)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)

        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        val db = AppDatabase.getDatabase(this)
        val userDao = db.UserDao()
        val sessionDao = db.sessionDao()

        // Verificar si ya hay una sesión activa
        CoroutineScope(Dispatchers.IO).launch {
            val session = sessionDao.getActiveSession()
            if (session != null) {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.putExtra("userId", session.userId)
                startActivity(intent)
                finish()
            }
        }
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.IO).launch {
                val user = userDao.getUserByEmail(email)

                runOnUiThread {
                    if (user != null && user.password == password) {
                        CoroutineScope(Dispatchers.IO).launch {
                            sessionDao.clearSession()
                            sessionDao.insertSession(Session(userId = user.id))

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("userId", user.id)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Correo o contraseña incorrectos",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}