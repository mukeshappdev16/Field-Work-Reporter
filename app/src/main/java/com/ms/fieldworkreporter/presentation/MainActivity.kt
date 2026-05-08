package com.ms.fieldworkreporter.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ms.fieldworkreporter.presentation.detail.TaskDetailScreen
import com.ms.fieldworkreporter.presentation.home.AddTaskDialog
import com.ms.fieldworkreporter.presentation.home.HomeScreen
import com.ms.fieldworkreporter.presentation.home.HomeViewModel
import com.ms.fieldworkreporter.presentation.settings.SettingsScreen
import com.ms.fieldworkreporter.ui.theme.FieldWorkReporterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FieldWorkReporterTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Routes.Home) {
        composable<Routes.Home> {
            MainScreen(
                homeViewModel = homeViewModel,
                onTaskClick = { task ->
                    navController.navigate(
                        Routes.TaskDetail(
                            id = task.id,
                            title = task.title,
                            description = task.description
                        )
                    )
                },
                onAddTask = { title, description ->
                    navController.navigate(
                        Routes.TaskDetail(
                            id = null,
                            title = title,
                            description = description
                        )
                    )
                }
            )
        }
        composable<Routes.TaskDetail> { backStackEntry ->
            val taskDetail: Routes.TaskDetail = backStackEntry.toRoute()
            TaskDetailScreen(
                taskId = taskDetail.id,
                taskTitle = taskDetail.title,
                taskDescription = taskDetail.description,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    onTaskClick: (com.ms.fieldworkreporter.domain.model.Task) -> Unit,
    onAddTask: (String, String) -> Unit
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Home", "Settings")
    val icons = listOf(Icons.Default.Home, Icons.Default.Settings)
    
    var showAddTaskDialog by remember { mutableStateOf(value = false) }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, description ->
                onAddTask(title, description)
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Reporter", 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                actions = {
                    if (selectedItem == 0) {
                        IconButton(
                            onClick = { showAddTaskDialog = true }
                        ) {
                            Icon(
                                Icons.Default.Add, 
                                contentDescription = "Add Task",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> HomeScreen(homeViewModel, onTaskClick = onTaskClick)
                1 -> SettingsScreen()
            }
        }
    }
}
