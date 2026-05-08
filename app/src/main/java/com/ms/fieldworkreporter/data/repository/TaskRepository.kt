package com.ms.fieldworkreporter.data.repository

import android.net.Uri
import com.ms.fieldworkreporter.data.local.dao.TaskDao
import com.ms.fieldworkreporter.data.local.entity.AttachmentEntity
import com.ms.fieldworkreporter.data.local.entity.AttachmentType
import com.ms.fieldworkreporter.data.local.entity.TaskEntity
import com.ms.fieldworkreporter.data.local.model.TaskWithDetails
import com.ms.fieldworkreporter.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasksWithDetails().map { list ->
            list.map { it.toTask() }
        }
    }

    suspend fun saveTask(
        id: Long? = null,
        title: String,
        description: String,
        photos: List<Uri>,
        notes: List<String>,
        voices: List<File>,
        isCompleted: Boolean = false
    ): Long {
        val taskEntity = if (id != null) {
            TaskEntity(id = id, title = title, description = description, isCompleted = isCompleted)
        } else {
            TaskEntity(title = title, description = description, isCompleted = isCompleted)
        }
        
        val taskId = taskDao.insertTask(taskEntity)
        
        // Clear existing attachments to avoid duplicates on multiple saves
        taskDao.deleteAttachmentsByTaskId(taskId)
        
        val attachments = mutableListOf<AttachmentEntity>()
        
        photos.forEach { 
            attachments.add(AttachmentEntity(taskId = taskId, type = AttachmentType.PHOTO, content = it.toString()))
        }
        
        notes.forEach { 
            attachments.add(AttachmentEntity(taskId = taskId, type = AttachmentType.TEXT, content = it))
        }
        
        voices.forEach { 
            attachments.add(AttachmentEntity(taskId = taskId, type = AttachmentType.VOICE, content = it.absolutePath))
        }
        
        if (attachments.isNotEmpty()) {
            taskDao.insertAttachments(attachments)
        }
        return taskId
    }

    suspend fun getTaskById(id: Long): Task? {
        return taskDao.getTaskWithDetailsById(id)?.toTask()
    }

    suspend fun getAttachmentsByTaskId(taskId: Long): List<AttachmentEntity> {
        return taskDao.getTaskWithDetailsById(taskId)?.attachments ?: emptyList()
    }

    suspend fun getTaskByTitleAndDescription(title: String, description: String): Task? {
        // This is a bit weak but works for now to find the "draft" we just started
        return taskDao.getAllTasksWithDetails()
            .map { list -> 
                list.find { it.task.title == title && it.task.description == description }?.toTask()
            }
            .firstOrNull()
    }

    private fun TaskWithDetails.toTask(): Task {
        return Task(
            id = task.id,
            title = task.title,
            description = task.description,
            isCompleted = task.isCompleted
        )
    }
}
