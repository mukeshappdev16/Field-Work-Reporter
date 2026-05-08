package com.ms.fieldworkreporter.presentation.detail

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ms.fieldworkreporter.util.FileUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Long? = null,
    taskTitle: String,
    taskDescription: String,
    onBackClick: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(taskId) {
        viewModel.setTaskId(taskId)
    }

    var showFabMenu by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current
    val isCompleted = viewModel.isCompleted
    val hasAttachments = viewModel.photos.isNotEmpty() || 
                       viewModel.textNotes.isNotEmpty() || 
                       viewModel.voiceNotes.isNotEmpty()
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { viewModel.addPhoto(it) }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = FileUtils.getNewImageUri(context, taskTitle)
            tempImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showVoiceDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        taskTitle, 
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (!isCompleted) {
                        TextButton(
                            onClick = {
                                viewModel.saveTask(taskTitle, taskDescription) {
                                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = hasAttachments
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (!isCompleted) {
                TaskActionSpeedDial(
                    isOpen = showFabMenu,
                    onToggle = { showFabMenu = !showFabMenu },
                    onAction = { action ->
                        showFabMenu = false
                        when (action) {
                            "photo" -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            "voice" -> recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            "note" -> showNoteDialog = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column {
                    SectionHeader("Description", Icons.AutoMirrored.Filled.Assignment)
                    Text(
                        text = taskDescription,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            item {
                Column {
                    SectionHeader("Photos", Icons.Default.PhotoLibrary)
                    PhotoGallery(
                        photos = viewModel.photos,
                        onImageClick = { selectedImageUri = it }
                    )
                }
            }

            item {
                Column {
                    SectionHeader("Voice Notes", Icons.Default.Mic)
                    VoiceNoteList(
                        voiceNotes = viewModel.voiceNotes,
                        currentlyPlaying = viewModel.currentlyPlayingFile,
                        onPlayClick = { viewModel.playVoiceNote(it) },
                        onDeleteClick = { viewModel.deleteVoiceNote(it) },
                        showDelete = !isCompleted
                    )
                }
            }

            item {
                Column {
                    SectionHeader("Text Notes", Icons.AutoMirrored.Filled.Notes)
                    TextNoteList(
                        notes = viewModel.textNotes,
                        onDeleteClick = { viewModel.deleteTextNote(it) },
                        showDelete = !isCompleted
                    )
                }
            }
            
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isCompleted) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "This task is completed. It cannot be updated.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.saveTask(taskTitle, taskDescription, complete = true) {
                                Toast.makeText(context, "Task Completed!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !isCompleted && hasAttachments,
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Complete Task", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.syncTask() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = isCompleted,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Initiate Sync", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    if (showNoteDialog) {
        NoteInputDialog(
            onDismiss = { showNoteDialog = false },
            onConfirm = { 
                viewModel.addTextNote(it)
                showNoteDialog = false
            }
        )
    }

    if (showVoiceDialog) {
        VoiceRecorderDialog(
            onDismiss = { showVoiceDialog = false },
            onStartRecording = { viewModel.startRecording(taskTitle) },
            onStopRecording = { 
                viewModel.stopRecording()
                showVoiceDialog = false
            }
        )
    }

    selectedImageUri?.let { uri ->
        ImagePreviewDialog(
            uri = uri,
            onDismiss = { selectedImageUri = null },
            onDelete = {
                viewModel.deletePhoto(uri)
                selectedImageUri = null
            },
            isEditable = !isCompleted
        )
    }
}

@Composable
fun ImagePreviewDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    isEditable: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Photo Preview") },
        text = {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        },
        confirmButton = {
            if (isEditable) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PhotoGallery(
    photos: List<Uri>,
    onImageClick: (Uri) -> Unit
) {
    if (photos.isEmpty()) {
        AddContentPlaceholder(Icons.Default.AddAPhoto, "No Photos Captured", isFullWidth = true)
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(photos) { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onImageClick(uri) },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun VoiceNoteList(
    voiceNotes: List<File>,
    currentlyPlaying: File?,
    onPlayClick: (File) -> Unit,
    onDeleteClick: (File) -> Unit,
    showDelete: Boolean = true
) {
    if (voiceNotes.isEmpty()) {
        AddContentPlaceholder(Icons.Default.Mic, "No Voice Notes", isFullWidth = true)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            voiceNotes.forEach { file ->
                val isPlaying = currentlyPlaying == file
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onPlayClick(file) }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Stop" else "Play"
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(file.name, style = MaterialTheme.typography.bodyMedium)
                            Text("${file.length() / 1024} KB", style = MaterialTheme.typography.labelSmall)
                        }
                        if (showDelete) {
                            IconButton(onClick = { onDeleteClick(file) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextNoteList(
    notes: List<String>,
    onDeleteClick: (String) -> Unit,
    showDelete: Boolean = true
) {
    if (notes.isEmpty()) {
        AddContentPlaceholder(Icons.Default.EditNote, "No Text Notes", isFullWidth = true)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            notes.forEach { note ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = note,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (showDelete) {
                            IconButton(onClick = { onDeleteClick(note) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteInputDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Note") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                placeholder = { Text("Type your note here...") }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun VoiceRecorderDialog(
    onDismiss: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = if (!isRecording) onDismiss else ({}),
        title = { Text(if (isRecording) "Recording..." else "Voice Recorder") },
        text = {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                if (isRecording) {
                    CircularProgressIndicator()
                } else {
                    Text("Press start to record your voice note")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isRecording) {
                        onStopRecording()
                    } else {
                        onStartRecording()
                        isRecording = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isRecording) "Stop & Save" else "Start Recording")
            }
        },
        dismissButton = {
            if (!isRecording) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
fun AddContentPlaceholder(icon: ImageVector, label: String, isFullWidth: Boolean = false) {
    Surface(
        modifier = if (isFullWidth) Modifier.fillMaxWidth() else Modifier.size(140.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TaskActionSpeedDial(
    isOpen: Boolean,
    onToggle: () -> Unit,
    onAction: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn() + expandVertically() + slideInVertically { it / 2 },
            exit = fadeOut() + shrinkVertically() + slideOutVertically { it / 2 }
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpeedDialItem(label = "Photo", icon = Icons.Default.CameraAlt) { onAction("photo") }
                SpeedDialItem(label = "Voice", icon = Icons.Default.Mic) { onAction("voice") }
                SpeedDialItem(label = "Note", icon = Icons.AutoMirrored.Filled.NoteAdd) { onAction("note") }
            }
        }

        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (isOpen) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Add content"
            )
        }
    }
}

@Composable
fun SpeedDialItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = CircleShape
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
        }
    }
}
