package com.ms.fieldworkreporter.presentation.detail

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskTitle: String,
    taskDescription: String,
    onBackClick: () -> Unit
) {
    var showFabMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(taskTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            TaskActionSpeedDial(
                isOpen = showFabMenu,
                onToggle = { showFabMenu = !showFabMenu },
                onAction = { action ->
                    showFabMenu = false
                    println("Action selected: $action")
                    // TODO: Handle actions in ViewModel
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader("Description")
                Text(
                    text = taskDescription,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                SectionHeader("Photos")
                PhotoGallery(emptyList()) // Placeholder
            }

            item {
                SectionHeader("Voice Notes")
                VoiceNoteList(emptyList()) // Placeholder
            }

            item {
                SectionHeader("Text Notes")
                TextNoteList(emptyList()) // Placeholder
            }
            
            // Extra space at bottom for FAB
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun PhotoGallery(photos: List<String>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AddContentPlaceholder(Icons.Default.AddAPhoto, "Add Photo")
        }
        items(photos) { photoUrl ->
            // Placeholder for an image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(photoUrl, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun VoiceNoteList(voiceNotes: List<String>) {
    if (voiceNotes.isEmpty()) {
        AddContentPlaceholder(Icons.Default.Mic, "No Voice Notes", isFullWidth = true)
    } else {
        // Implementation for voice note items
    }
}

@Composable
fun TextNoteList(notes: List<String>) {
    if (notes.isEmpty()) {
        AddContentPlaceholder(Icons.Default.EditNote, "No Text Notes", isFullWidth = true)
    } else {
        // Implementation for text note items
    }
}

@Composable
fun AddContentPlaceholder(icon: ImageVector, label: String, isFullWidth: Boolean = false) {
    OutlinedCard(
        modifier = if (isFullWidth) Modifier.fillMaxWidth() else Modifier.size(100.dp),
        onClick = { /* TODO */ },
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
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
