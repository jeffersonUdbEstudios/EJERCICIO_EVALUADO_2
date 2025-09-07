package com.example.ejercicio_evaluado_2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    
    private lateinit var btnGoToLogin: Button
    private lateinit var btnGoToRegister: Button
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        
        // Verificar si hay un usuario autenticado
        checkUserAuthentication()
        
        initViews()
        setupClickListeners()
    }
    
    private fun checkUserAuthentication() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Si hay un usuario autenticado, ir directamente a Home
            navigateToHome()
        }
    }
    
    private fun navigateToHome() {
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
        finish() // Cerrar MainActivity para que no quede en el stack
    }
    
    private fun initViews() {
        btnGoToLogin = findViewById(R.id.btnGoToLogin)
        btnGoToRegister = findViewById(R.id.btnGoToRegister)
    }
    
    private fun setupClickListeners() {
        btnGoToLogin.setOnClickListener {
            navigateToLogin()
        }
        
        btnGoToRegister.setOnClickListener {
            navigateToRegister()
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}