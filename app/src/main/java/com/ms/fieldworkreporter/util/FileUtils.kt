package com.ms.fieldworkreporter.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    fun generateFileName(taskName: String, type: String, extension: String): String {
        val datePart = SimpleDateFormat("dMMM_HH'H'_mm'M'_ss'Sec'", Locale.getDefault()).format(Date())
        val randomPart = (10000..99999).random()
        val sanitizedTaskName = taskName.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "")
        return "${sanitizedTaskName}_${type}_${datePart}_$randomPart$extension"
    }

    fun getNewImageUri(context: Context, taskName: String): Uri {
        val fileName = generateFileName(taskName, "Image", ".jpg")
        val storageDir: File? = context.getExternalFilesDir("Pictures")
        val file = File(storageDir, fileName)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
