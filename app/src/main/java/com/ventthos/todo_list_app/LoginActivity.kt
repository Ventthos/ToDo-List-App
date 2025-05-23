package com.ventthos.todo_list_app

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
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

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.get_email_login)
        passwordInput = findViewById(R.id.get_pass_login)
        loginButton = findViewById(R.id.Login_button)
        registerButton = findViewById(R.id.signup_button)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)

        database = FirebaseDatabase.getInstance().getReference("users")

        val sessionDao = AppDatabase.getDatabase(this).sessionDao()

        // Si ya hay sesión, redirigir
        CoroutineScope(Dispatchers.IO).launch {
            val session = sessionDao.getActiveSession()
            if (session != null) {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
                    putExtra("userId", session.userId)
                })
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

            // Buscar en Firebase
            database.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (userSnapshot in snapshot.children) {
                                val dbPassword = userSnapshot.child("password").getValue(String::class.java)
                                if (dbPassword == password) {
                                    val userId = userSnapshot.key!!.toIntOrNull() ?: userSnapshot.key.hashCode()

                                    // Guardar sesión local
                                    CoroutineScope(Dispatchers.IO).launch {
                                        sessionDao.clearSession()
                                        sessionDao.insertSession(Session(userId = userId))

                                        runOnUiThread {
                                            startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
                                                putExtra("userId", userId)
                                            })
                                            finish()
                                        }
                                    }
                                    return
                                } else {
                                    Toast.makeText(this@LoginActivity, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                                    return
                                }
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@LoginActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
}
