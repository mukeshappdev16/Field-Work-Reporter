package com.ms.fieldworkreporter.data.repository

import android.net.Uri
import com.ms.fieldworkreporter.data.local.dao.TaskDao
import com.ms.fieldworkreporter.data.local.entity.AttachmentEntity
import com.ms.fieldworkreporter.data.local.entity.AttachmentType
import com.ms.fieldworkreporter.data.local.entity.TaskEntity
import com.ms.fieldworkreporter.data.local.model.TaskWithDetails
import com.ms.fieldworkreporter.domain.model.Task
import kotlinx.coroutines.flow.Flow
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

    suspend fun saveTask(title: String, description: String, photos: List<Uri>, notes: List<String>, voices: List<File>) {
        val taskId = taskDao.insertTask(TaskEntity(title = title, description = description))
        
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
    }

    private fun TaskWithDetails.toTask(): Task {
        return Task(
            id = task.id.toInt(),
            title = task.title,
            description = task.description
            // In a more complete app, we'd include attachments in the domain model too
        )
    }
}
