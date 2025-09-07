package com.example.ejercicio_evaluado_2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var cbTerms: CheckBox
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    
    // Firebase Auth
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        initViews()
        setupClickListeners()
    }
    
    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        cbTerms = findViewById(R.id.cbTerms)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
    }
    
    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            performRegister()
        }
        
        tvLogin.setOnClickListener {
            navigateToLogin()
        }
    }
    
    private fun performRegister() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        
        // Limpiar errores anteriores
        clearFieldErrors()
        
        // Validaciones básicas
        if (!validateRegisterFields(fullName, email, password, confirmPassword)) {
            return
        }
        
        // Deshabilitar el botón para evitar múltiples registros
        setRegisterButtonState(false, "Creando cuenta...")
        
        // Crear usuario con Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setRegisterButtonState(true, "Crear Cuenta")
                
                if (task.isSuccessful) {
                    handleRegisterSuccess(fullName, email)
                } else {
                    handleRegisterError()
                }
            }
    }
    
    private fun validateRegisterFields(fullName: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true
        
        if (fullName.isEmpty()) {
            etFullName.error = "Campo requerido"
            etFullName.requestFocus()
            isValid = false
        } else if (fullName.length < 2) {
            etFullName.error = "Mínimo 2 caracteres"
            etFullName.requestFocus()
            isValid = false
        }
        
        if (email.isEmpty()) {
            etEmail.error = "Campo requerido"
            if (isValid) etEmail.requestFocus()
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Formato inválido"
            if (isValid) etEmail.requestFocus()
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
        } else if (!isPasswordStrong(password)) {
            etPassword.error = "Debe contener letras y números"
            if (isValid) etPassword.requestFocus()
            isValid = false
        }
        
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Campo requerido"
            if (isValid) etConfirmPassword.requestFocus()
            isValid = false
        } else if (password != confirmPassword) {
            etConfirmPassword.error = "Las contraseñas no coinciden"
            if (isValid) etConfirmPassword.requestFocus()
            isValid = false
        }
        
        if (!cbTerms.isChecked) {
            showErrorMessage("Debes aceptar los términos y condiciones")
            isValid = false
        }
        
        return isValid
    }
    
    private fun isPasswordStrong(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }
    
    private fun handleRegisterSuccess(fullName: String, email: String) {
        val user: FirebaseUser? = auth.currentUser
        user?.let {
            // Actualizar el perfil del usuario con el nombre
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()
            
            it.updateProfile(profileUpdates)
                .addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        showSuccessMessage("¡Registro exitoso! Bienvenido $fullName")
                        
                        // Navegar al login después del registro exitoso
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        showErrorMessage("Algo salió mal, por favor inténtalo de nuevo")
                    }
                }
        }
    }
    
    private fun handleRegisterError() {
        showErrorMessage("Algo salió mal, por favor inténtalo de nuevo")
        
        // Limpiar campos de contraseña en caso de error
        etPassword.setText("")
        etConfirmPassword.setText("")
        etPassword.requestFocus()
    }
    
    private fun setRegisterButtonState(enabled: Boolean, text: String) {
        btnRegister.isEnabled = enabled
        btnRegister.text = text
    }
    
    private fun clearFieldErrors() {
        etFullName.error = null
        etEmail.error = null
        etPassword.error = null
        etConfirmPassword.error = null
    }
    
    private fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun showInfoMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}