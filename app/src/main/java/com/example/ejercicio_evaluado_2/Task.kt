package com.example.ejercicio_evaluado_2

import com.google.firebase.database.Exclude
import java.text.SimpleDateFormat
import java.util.*

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val status: String = TaskStatus.PENDING.name,
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = ""
) {
    @Exclude
    fun getFormattedDate(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return "Creado: ${formatter.format(Date(createdAt))}"
    }
    
    @Exclude
    fun getTaskStatus(): TaskStatus {
        return try {
            TaskStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            TaskStatus.PENDING
        }
    }
    
    @Exclude
    fun isCompleted(): Boolean {
        return getTaskStatus() == TaskStatus.COMPLETED
    }
    
    @Exclude
    fun isPending(): Boolean {
        return getTaskStatus() == TaskStatus.PENDING
    }
    
    // Constructor sin parámetros para Firebase Database
    constructor() : this("", "", "", TaskStatus.PENDING.name, System.currentTimeMillis(), "")
}

enum class TaskStatus(val displayName: String) {
    PENDING("Pendiente"),
    COMPLETED("Completada")
} 