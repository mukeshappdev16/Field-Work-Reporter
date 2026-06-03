package com.ms.fieldworkreporter.data.local.dao

import androidx.room.*
import com.ms.fieldworkreporter.data.local.entity.AttachmentEntity
import com.ms.fieldworkreporter.data.local.entity.TaskEntity
import com.ms.fieldworkreporter.data.local.model.TaskWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachments: List<AttachmentEntity>)

    @Transaction
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasksWithDetails(): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskWithDetailsById(taskId: Long): TaskWithDetails?

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM task_attachments WHERE taskId = :taskId")
    suspend fun deleteAttachmentsByTaskId(taskId: Long)

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsyncedTasksWithDetails(): List<TaskWithDetails>

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Update
    suspend fun updateAttachments(attachments: List<AttachmentEntity>)
}
