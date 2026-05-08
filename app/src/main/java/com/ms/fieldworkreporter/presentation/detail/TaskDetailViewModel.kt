package com.ms.fieldworkreporter.presentation.detail

import android.app.Application
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ms.fieldworkreporter.data.local.entity.AttachmentType
import com.ms.fieldworkreporter.data.repository.TaskRepository
import com.ms.fieldworkreporter.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val application: Application
) : ViewModel() {

    val photos = mutableStateListOf<Uri>()
    val textNotes = mutableStateListOf<String>()
    val voiceNotes = mutableStateListOf<File>()

    var isCompleted by mutableStateOf(false)
        private set

    private var taskId: Long? = null

    private var mediaRecorder: MediaRecorder? = null
    private var currentVoiceFile: File? = null

    private var mediaPlayer: MediaPlayer? = null
    var currentlyPlayingFile by mutableStateOf<File?>(null)
        private set

    fun setTaskId(id: Long?) {
        if (id != null && taskId == null) {
            taskId = id
            loadTaskDetails(id)
        }
    }

    private fun loadTaskDetails(id: Long) {
        viewModelScope.launch {
            repository.getTaskById(id)?.let { task ->
                isCompleted = task.isCompleted
            }
            
            repository.getAttachmentsByTaskId(id).forEach { attachment ->
                when (attachment.type) {
                    AttachmentType.PHOTO -> photos.add(Uri.parse(attachment.content))
                    AttachmentType.VOICE -> voiceNotes.add(File(attachment.content))
                    AttachmentType.TEXT -> textNotes.add(attachment.content)
                }
            }
        }
    }

    fun saveTask(title: String, description: String, complete: Boolean = false, onSaved: () -> Unit) {
        viewModelScope.launch {
            val newId = repository.saveTask(
                id = taskId,
                title = title,
                description = description,
                photos = photos.toList(),
                notes = textNotes.toList(),
                voices = voiceNotes.toList(),
                isCompleted = isCompleted || complete
            )
            taskId = newId
            if (complete) isCompleted = true
            onSaved()
        }
    }

    fun syncTask() {
        // Placeholder for sync logic
    }

    fun addTextNote(note: String) {
        if (note.isNotBlank()) {
            textNotes.add(note)
        }
    }

    fun addPhoto(uri: Uri) {
        photos.add(uri)
    }

    fun deletePhoto(uri: Uri) {
        photos.remove(uri)
    }

    fun deleteTextNote(note: String) {
        textNotes.remove(note)
    }

    fun deleteVoiceNote(file: File) {
        if (file.exists()) {
            file.delete()
        }
        voiceNotes.remove(file)
    }

    fun startRecording(taskName: String) {
        val fileName = FileUtils.generateFileName(taskName, "Voice", ".m4a")
        val storageDir: File? = application.getExternalFilesDir("Audio")
        if (storageDir?.exists() == false) storageDir.mkdirs()
        
        currentVoiceFile = File(storageDir, fileName)
        
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(application)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(currentVoiceFile?.absolutePath)
            prepare()
            start()
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        currentVoiceFile?.let {
            voiceNotes.add(it)
        }
        currentVoiceFile = null
    }

    fun playVoiceNote(file: File) {
        if (currentlyPlayingFile == file) {
            stopPlaying()
            return
        }

        stopPlaying()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
            setOnCompletionListener {
                stopPlaying()
            }
        }
        currentlyPlayingFile = file
    }

    fun stopPlaying() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        currentlyPlayingFile = null
    }

    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
        mediaPlayer?.release()
    }
}
