package com.ms.fieldworkreporter.sync

import android.content.Context
import android.util.Base64
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.ms.fieldworkreporter.data.local.dao.TaskDao
import com.ms.fieldworkreporter.data.local.entity.AttachmentType
import com.ms.fieldworkreporter.data.local.model.TaskWithDetails
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import java.io.File

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val taskDao: TaskDao,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val unsyncedTasks = taskDao.getUnsyncedTasksWithDetails()
        
        for (taskWithDetails in unsyncedTasks) {
            try {
                syncTask(taskWithDetails)
            } catch (e: Exception) {
                e.printStackTrace()
                return Result.retry()
            }
        }
        
        return Result.success()
    }

    private suspend fun syncTask(taskWithDetails: TaskWithDetails) {
        val task = taskWithDetails.task
        val attachments = taskWithDetails.attachments
        
        val updatedAttachments = attachments.map { attachment ->
            if (attachment.remoteUrl == null && (attachment.type == AttachmentType.PHOTO || attachment.type == AttachmentType.VOICE)) {
                val base64Data = encodeFileToBase64(attachment.content)
                attachment.copy(remoteUrl = base64Data)
            } else {
                attachment
            }
        }
        
        val firestoreData = mutableMapOf<String, Any>(
            "title" to task.title,
            "description" to task.description,
            "isCompleted" to task.isCompleted,
            "createdAt" to task.createdAt,
            "attachments" to updatedAttachments.map { 
                mapOf(
                    "type" to it.type.name,
                    "content" to it.content,
                    "mediaData" to (it.remoteUrl ?: "")
                )
            }
        )
        
        firestore.collection("tasks").document(task.id.toString())
            .set(firestoreData)
            .await()
            
        taskDao.updateTask(task.copy(isSynced = true))
        taskDao.updateAttachments(updatedAttachments)
    }

    private fun encodeFileToBase64(path: String): String? {
        return try {
            val file = File(path)
            if (!file.exists()) return null
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
