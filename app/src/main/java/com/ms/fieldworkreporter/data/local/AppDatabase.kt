package com.ms.fieldworkreporter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ms.fieldworkreporter.data.local.dao.TaskDao
import com.ms.fieldworkreporter.data.local.entity.AttachmentEntity
import com.ms.fieldworkreporter.data.local.entity.TaskEntity

@Database(entities = [TaskEntity::class, AttachmentEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao
}
