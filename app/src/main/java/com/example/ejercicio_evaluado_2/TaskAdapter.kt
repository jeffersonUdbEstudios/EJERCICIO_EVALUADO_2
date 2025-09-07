package com.example.ejercicio_evaluado_2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private var tasks: MutableList<Task>,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTaskTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvTaskDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val tvTaskStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
        val tvTaskDate: TextView = itemView.findViewById(R.id.tvTaskDate)
        val btnEditTask: Button = itemView.findViewById(R.id.btnEditTask)
        val btnDeleteTask: Button = itemView.findViewById(R.id.btnDeleteTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        
        holder.tvTaskTitle.text = task.title
        holder.tvTaskDescription.text = task.description
        holder.tvTaskStatus.text = "Pendiente"
        holder.tvTaskDate.text = task.getFormattedDate()
        
        holder.btnEditTask.setOnClickListener {
            onEditClick(task)
        }
        
        holder.btnDeleteTask.setOnClickListener {
            onDeleteClick(task)
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
} 