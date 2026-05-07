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
import androidx.lifecycle.AndroidViewModel
import com.ms.fieldworkreporter.util.FileUtils
import java.io.File

class TaskDetailViewModel(application: Application) : AndroidViewModel(application) {

    val photos = mutableStateListOf<Uri>()
    val textNotes = mutableStateListOf<String>()
    val voiceNotes = mutableStateListOf<File>()

    private var mediaRecorder: MediaRecorder? = null
    private var currentVoiceFile: File? = null

    private var mediaPlayer: MediaPlayer? = null
    var currentlyPlayingFile by mutableStateOf<File?>(null)
        private set

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
        val storageDir: File? = getApplication<Application>().getExternalFilesDir("Audio")
        if (storageDir?.exists() == false) storageDir.mkdirs()
        
        currentVoiceFile = File(storageDir, fileName)
        
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(getApplication())
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
