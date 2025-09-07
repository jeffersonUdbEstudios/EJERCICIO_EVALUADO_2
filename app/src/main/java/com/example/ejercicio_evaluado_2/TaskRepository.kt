package com.example.ejercicio_evaluado_2

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.auth.FirebaseAuth

class TaskRepository {
    
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    fun addTask(task: Task, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = getCurrentUserId()
        if (userId == null) {
            onError("Algo salió mal, por favor inténtalo de nuevo")
            return
        }
        
        val taskWithId = task.copy(id = database.child("tasks").child(userId).push().key ?: "")
        
        database.child("tasks").child(userId).child(taskWithId.id)
            .setValue(taskWithId)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError("Algo salió mal, por favor inténtalo de nuevo")
            }
    }
    
    fun updateTask(task: Task, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = getCurrentUserId()
        if (userId == null) {
            onError("Algo salió mal, por favor inténtalo de nuevo")
            return
        }
        
        database.child("tasks").child(userId).child(task.id)
            .setValue(task)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError("Algo salió mal, por favor inténtalo de nuevo")
            }
    }
    
    fun deleteTask(taskId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = getCurrentUserId()
        if (userId == null) {
            onError("Algo salió mal, por favor inténtalo de nuevo")
            return
        }
        
        database.child("tasks").child(userId).child(taskId)
            .removeValue()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError("Algo salió mal, por favor inténtalo de nuevo")
            }
    }
    
    fun getTasks(onSuccess: (List<Task>) -> Unit, onError: (String) -> Unit) {
        val userId = getCurrentUserId()
        if (userId == null) {
            onError("Algo salió mal, por favor inténtalo de nuevo")
            return
        }
        
        database.child("tasks").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = mutableListOf<Task>()
                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        task?.let { 
                            // Solo agregar tareas pendientes
                            if (it.status == TaskStatus.PENDING.name) {
                                tasks.add(it)
                            }
                        }
                    }
                    onSuccess(tasks)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    onError("Algo salió mal, por favor inténtalo de nuevo")
                }
            })
    }
    
    fun completeTask(taskId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = getCurrentUserId()
        if (userId == null) {
            onError("Algo salió mal, por favor inténtalo de nuevo")
            return
        }
        
        // En lugar de actualizar el estado, eliminar la tarea completamente
        database.child("tasks").child(userId).child(taskId)
            .removeValue()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError("Algo salió mal, por favor inténtalo de nuevo")
            }
    }
}