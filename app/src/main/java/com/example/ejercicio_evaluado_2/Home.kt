package com.example.ejercicio_evaluado_2

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Home : AppCompatActivity() {
    
    private lateinit var tvWelcome: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnAddTask: Button
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var layoutEmptyTasks: LinearLayout
    
    // Firebase Auth
    private lateinit var auth: FirebaseAuth
    
    // Firebase Database
    private lateinit var database: DatabaseReference
    
    // Tasks
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private lateinit var taskRepository: TaskRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Inicializar Firebase Auth y Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        taskRepository = TaskRepository()
        
        initViews()
        setupRecyclerView()
        setupClickListeners()
        displayUserInfo()
        loadTasks()
    }
    
    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnLogout = findViewById(R.id.btnLogout)
        btnAddTask = findViewById(R.id.btnAddTask)
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks)
        layoutEmptyTasks = findViewById(R.id.layoutEmptyTasks)
    }
    
    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            tasks = tasks,
            onEditClick = { task -> showTaskDialog(task) },
            onDeleteClick = { task -> showDeleteTaskDialog(task) }
        )
        
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        recyclerViewTasks.adapter = taskAdapter
    }
    
    private fun setupClickListeners() {
        btnLogout.setOnClickListener {
            performLogout()
        }
        
        btnAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }
    
    private fun displayUserInfo() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userName = currentUser.displayName ?: "Usuario"
            tvWelcome.text = "Bienvenido, $userName"
        } else {
            // Si no hay usuario autenticado, mostrar mensaje genérico
            tvWelcome.text = "Bienvenido"
        }
    }
    
    private fun loadTasks() {
        taskRepository.getTasks(
            onSuccess = { taskList ->
                tasks.clear()
                tasks.addAll(taskList)
                taskAdapter.notifyDataSetChanged()
                updateEmptyState()
            },
            onError = { error ->
                showErrorMessage("Algo salió mal, por favor inténtalo de nuevo")
                updateEmptyState()
            }
        )
    }
    
    private fun showAddTaskDialog() {
        showTaskDialog(null)
    }
    
    private fun showEditTaskDialog(task: Task) {
        showTaskDialog(task)
    }
    
    private fun showTaskDialog(task: Task?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_task, null)
        
        val tvDialogTitle: TextView = dialogView.findViewById(R.id.tvDialogTitle)
        val etTaskTitle: EditText = dialogView.findViewById(R.id.etTaskTitle)
        val etTaskDescription: EditText = dialogView.findViewById(R.id.etTaskDescription)
        val cbMarkAsCompleted: CheckBox = dialogView.findViewById(R.id.cbMarkAsCompleted)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)
        val btnSave: Button = dialogView.findViewById(R.id.btnSave)
        
        // Configurar el diálogo según el modo
        if (task != null) {
            // Modo edición
            tvDialogTitle.text = "Editar Tarea"
            etTaskTitle.setText(task.title)
            etTaskDescription.setText(task.description)
            cbMarkAsCompleted.visibility = View.VISIBLE
        } else {
            // Modo nueva tarea
            tvDialogTitle.text = "Nueva Tarea"
            cbMarkAsCompleted.visibility = View.GONE
        }
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnSave.setOnClickListener {
            val title = etTaskTitle.text.toString().trim()
            val description = etTaskDescription.text.toString().trim()
            
            if (title.isEmpty()) {
                etTaskTitle.error = "Campo requerido"
                return@setOnClickListener
            }
            
            if (description.isEmpty()) {
                etTaskDescription.error = "Campo requerido"
                return@setOnClickListener
            }
            
            if (task == null) {
                // Nueva tarea - siempre se crea como PENDIENTE
                val newTask = Task(
                    title = title,
                    description = description,
                    status = TaskStatus.PENDING.name
                )
                
                taskRepository.addTask(
                    newTask,
                    onSuccess = {
                        showSuccessMessage("Tarea agregada exitosamente")
                        dialog.dismiss()
                    },
                    onError = { error ->
                        showErrorMessage("Algo salió mal, por favor inténtalo de nuevo")
                    }
                )
            } else {
                // Editar tarea existente
                if (cbMarkAsCompleted.isChecked) {
                    // Si se marca como completada, eliminar la tarea
                    taskRepository.completeTask(
                        taskId = task.id,
                        onSuccess = {
                            showSuccessMessage("Tarea completada")
                            dialog.dismiss()
                        },
                        onError = { error ->
                            showErrorMessage("Algo salió mal, por favor inténtalo de nuevo")
                        }
                    )
                } else {
                    // Actualizar la tarea normalmente
                    val updatedTask = task.copy(
                        title = title,
                        description = description
                    )
                    
                    taskRepository.updateTask(
                        updatedTask,
                        onSuccess = {
                            showSuccessMessage("Tarea actualizada exitosamente")
                            dialog.dismiss()
                        },
                        onError = { error ->
                            showErrorMessage("Algo salió mal, por favor inténtalo de nuevo")
                        }
                    )
                }
            }
        }
        
        dialog.show()
    }
    
    private fun completeTask(task: Task) {
        taskRepository.completeTask(
            taskId = task.id,
            onSuccess = {
                showSuccessMessage("Tarea completada")
                loadTasks() // Recargar la lista para reflejar los cambios
            },
            onError = { error ->
                showErrorMessage("Algo salió mal, por favor inténtalo de nuevo")
            }
        )
    }
    
    private fun showDeleteTaskDialog(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Tarea")
            .setMessage("¿Estás seguro de que quieres eliminar la tarea '${task.title}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                taskRepository.deleteTask(
                    task.id,
                    onSuccess = {
                        showSuccessMessage("Tarea eliminada exitosamente")
                    },
                    onError = { error ->
                        showErrorMessage("Algo salió mal, por favor inténtalo de nuevo")
                    }
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun updateEmptyState() {
        if (tasks.isEmpty()) {
            recyclerViewTasks.visibility = View.GONE
            layoutEmptyTasks.visibility = View.VISIBLE
        } else {
            recyclerViewTasks.visibility = View.VISIBLE
            layoutEmptyTasks.visibility = View.GONE
        }
    }
    
    private fun performLogout() {
        auth.signOut()
        showSuccessMessage("Sesión cerrada exitosamente")
        navigateToLogin()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}