package com.ms.fieldworkreporter.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ms.fieldworkreporter.data.local.entity.AttachmentEntity
import com.ms.fieldworkreporter.data.local.entity.TaskEntity

data class TaskWithDetails(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val attachments: List<AttachmentEntity>
)
