package com.example.ejercicio_evaluado_2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    
    // Firebase Auth
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        initViews()
        setupClickListeners()
        handleIntentData()
    }
    
    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
    }
    
    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            performLogin()
        }
        
        tvRegister.setOnClickListener {
            navigateToRegister()
        }
    }
    
    private fun handleIntentData() {
        // Si viene de registro, pre-llenar el campo de email
        val emailFromRegister = intent.getStringExtra("email")
        if (!emailFromRegister.isNullOrEmpty()) {
            etEmail.setText(emailFromRegister)
            showSuccessMessage("Registro exitoso. Puedes iniciar sesión.")
        }
    }
    
    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        
        // Limpiar errores anteriores
        clearFieldErrors()
        
        // Validaciones básicas
        if (!validateLoginFields(email, password)) {
            return
        }
        
        // Deshabilitar el botón para evitar múltiples intentos
        setLoginButtonState(false, "Iniciando sesión...")
        
        // Iniciar sesión con Firebase Auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setLoginButtonState(true, "Iniciar Sesión")
                
                if (task.isSuccessful) {
                    handleLoginSuccess()
                } else {
                    handleLoginError()
                }
            }
    }
    
    private fun validateLoginFields(email: String, password: String): Boolean {
        var isValid = true
        
        if (email.isEmpty()) {
            etEmail.error = "Campo requerido"
            etEmail.requestFocus()
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Formato inválido"
            etEmail.requestFocus()
            isValid = false
        }
        
        if (password.isEmpty()) {
            etPassword.error = "Campo requerido"
            if (isValid) etPassword.requestFocus()
            isValid = false
        } else if (password.length < 6) {
            etPassword.error = "Mínimo 6 caracteres"
            if (isValid) etPassword.requestFocus()
            isValid = false
        }
        
        return isValid
    }
    
    private fun handleLoginSuccess() {
        val user: FirebaseUser? = auth.currentUser
        user?.let {
            val userName = it.displayName ?: "Usuario"
            showSuccessMessage("¡Bienvenido $userName!")
            
            // Navegar a la pantalla principal
            val intent = Intent(this, Home::class.java)
            intent.putExtra("userName", userName)
            intent.putExtra("userEmail", it.email)
            startActivity(intent)
            finish()
        }
    }
    
    private fun handleLoginError() {
        showErrorMessage("Algo salió mal, por favor inténtalo de nuevo")
        
        // Limpiar campos de contraseña en caso de error
        etPassword.setText("")
        etPassword.requestFocus()
    }
    
    private fun setLoginButtonState(enabled: Boolean, text: String) {
        btnLogin.isEnabled = enabled
        btnLogin.text = text
    }
    
    private fun clearFieldErrors() {
        etEmail.error = null
        etPassword.error = null
    }
    
    private fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}