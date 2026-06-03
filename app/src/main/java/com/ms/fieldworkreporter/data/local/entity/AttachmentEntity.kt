package com.ms.fieldworkreporter.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_attachments",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val type: AttachmentType,
    val content: String, // URI, File Path, or Note Text
    val remoteUrl: String? = null // Firebase Storage URL
)

enum class AttachmentType {
    PHOTO, VOICE, TEXT
}
