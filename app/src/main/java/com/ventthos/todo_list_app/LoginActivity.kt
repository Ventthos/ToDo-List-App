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
import com.google.firebase.database.*
import com.ventthos.todo_list_app.db.dataclasses.User

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

        val db = AppDatabase.getDatabase(this)
        val userDao = db.UserDao()
        val sessionDao = db.sessionDao()

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        // Verificar si ya hay sesión
        CoroutineScope(Dispatchers.IO).launch {
            val session = sessionDao.getActiveSession()
            if (session != null) {
                runOnUiThread {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
                        putExtra("userId", session.userId)
                    })
                    finish()
                }
            }
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

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

            val db = AppDatabase.getDatabase(this)
            val userDao = db.UserDao()
            val sessionDao = db.sessionDao()
            val database = FirebaseDatabase.getInstance()
            val usersRef = database.getReference("users")

            usersRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (userSnap in snapshot.children) {
                                val dbPassword = userSnap.child("password").getValue(String::class.java) ?: ""
                                if (dbPassword == password) {
                                    val remoteId = userSnap.key!!
                                    val name = userSnap.child("name").getValue(String::class.java) ?: ""
                                    val lastName = userSnap.child("lastName").getValue(String::class.java) ?: ""
                                    val avatarName = userSnap.child("avatar").getValue(String::class.java) ?: "mark"
                                    val lastPage = userSnap.child("lastPage").getValue(Int::class.java) ?: 0
                                    val localId = userSnap.child("localId").getValue(Int::class.java) ?: -1
                                    val avatarId = resources.getIdentifier(avatarName, "drawable", packageName)

                                    CoroutineScope(Dispatchers.IO).launch {
                                        // Si ya existe, evitamos duplicarlo
                                        val existing = userDao.getUserById(localId)
                                        if (existing == null) {
                                            val newUser = User(
                                                id = localId,
                                                name = name,
                                                lastName = lastName,
                                                email = email,
                                                password = password,
                                                avatar = avatarId,
                                                lastPage = lastPage,
                                                remoteId = remoteId
                                            )
                                            userDao.insertUser(newUser)
                                        }

                                        sessionDao.clearSession()
                                        sessionDao.insertSession(Session(userId = localId))

                                        runOnUiThread {
                                            startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
                                                putExtra("userId", localId)
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