package com.ventthos.todo_list_app

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.ventthos.todo_list_app.db.AppDatabase.AppDatabase
import com.ventthos.todo_list_app.db.dataclasses.User
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.ventthos.todo_list_app.db.dataclasses.TaskList

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var avatarImage: ImageView
    private lateinit var avatarText: TextView
    private lateinit var googleSignInButton: SignInButton
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var avatarDialog: Dialog
    private var selectedAvatarResId: Int = R.drawable.mark
    private val RC_SIGN_IN = 1001

    private val avatarList = listOf(
        R.drawable.xmen,
        R.drawable.wolverine,
        R.drawable.vision,
        R.drawable.spiderman,
        R.drawable.spawn,
        R.drawable.ironman,
        R.drawable.deadpool,
        R.drawable.daredevil,
        R.drawable.cyclops,
        R.drawable.capitanamerica,
        R.drawable.blackpanter,
        R.drawable.hierro
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        nameEditText = findViewById(R.id.register_name)
        lastNameEditText = findViewById(R.id.register_lastname)
        emailEditText = findViewById(R.id.register_email)
        passwordEditText = findViewById(R.id.register_password)
        confirmPasswordEditText = findViewById(R.id.register_confirm_password)
        registerButton = findViewById(R.id.button_register)
        avatarImage = findViewById(R.id.avatarImage)
        avatarText = findViewById(R.id.avatarText)
        googleSignInButton = findViewById(R.id.googleSignInButton)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInButton.setOnClickListener {
            // pa que cierre sesión si o si alv
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }

        avatarImage.setOnClickListener { showAvatarDialog() }
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (name.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
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
            if (password != confirmPassword) {
                Toast.makeText(this, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "todo_list_database"
            )
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()

            val userDao = db.UserDao()
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUser = User(
                name = name,
                lastName = lastName,
                email = email,
                password = password,
                avatar = selectedAvatarResId
            )

            val userId = userDao.insertUser(newUser).toInt()

            val taskListDao = db.TaskListDao()
   
            val red = basicColors[0]
            val idColor = red.colorId

            // Lista "pendientes" default al crear el usuario
            val defaultList = TaskList(
                name = "Pendientes",
                color = idColor,
                iconName = "time",
                iconId = R.drawable.time,
                userId = userId
            )

            taskListDao.addList(defaultList)

            val createdUser = db.UserDao().getUserByEmail(email)
            Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("userId", createdUser?.id ?: -1)
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)

                val fullName = account.displayName ?: ""
                val givenName = account.givenName ?: ""
                val familyName = account.familyName ?: ""

                nameEditText.setText(givenName.ifEmpty { fullName.split(" ").firstOrNull() ?: "" })
                lastNameEditText.setText(familyName.ifEmpty { fullName.split(" ").drop(1).joinToString(" ") })
                emailEditText.setText(account.email)

            } catch (e: ApiException) {
                Toast.makeText(this, "Error al iniciar con Google: ${e.statusCode}", Toast.LENGTH_SHORT).show()
                Log.e("GoogleSignIn", "Fallo: ", e)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedAvatarResId", selectedAvatarResId)
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        selectedAvatarResId = savedInstanceState.getInt("selectedAvatarResId", R.drawable.mark)
        avatarImage.setImageResource(selectedAvatarResId)
    }
    private fun showAvatarDialog() {
        avatarDialog = Dialog(this)
        avatarDialog.setContentView(R.layout.dialog_avatar_picker)
        avatarDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val avatarGrid = avatarDialog.findViewById<GridView>(R.id.avatarGrid)

        avatarGrid.adapter = object : BaseAdapter() {
            override fun getCount(): Int = avatarList.size
            override fun getItem(position: Int): Any = avatarList[position]
            override fun getItemId(position: Int): Long = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val imageView = ImageView(this@RegisterActivity)
                imageView.layoutParams = AbsListView.LayoutParams(200, 200)
                imageView.setImageResource(avatarList[position])
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setBackgroundResource(R.drawable.circle_background)

                imageView.setOnClickListener {
                    avatarImage.setImageResource(avatarList[position])
                    selectedAvatarResId = avatarList[position]
                    avatarDialog.dismiss()
                }
                return imageView
            }
        }
        avatarDialog.show()
    }
}