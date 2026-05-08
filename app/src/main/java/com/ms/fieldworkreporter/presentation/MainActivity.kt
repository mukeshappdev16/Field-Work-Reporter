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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ms.fieldworkreporter.presentation.detail.TaskDetailScreen
import com.ms.fieldworkreporter.presentation.home.AddTaskDialog
import com.ms.fieldworkreporter.presentation.home.HomeScreen
import com.ms.fieldworkreporter.presentation.home.HomeViewModel
import com.ms.fieldworkreporter.presentation.settings.SettingsScreen
import com.ms.fieldworkreporter.ui.theme.FieldWorkReporterTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                homeViewModel = homeViewModel,
            ) { task ->
                val encodedTitle =
                    URLEncoder.encode(task.title, StandardCharsets.UTF_8.toString())
                val encodedDesc =
                    URLEncoder.encode(task.description, StandardCharsets.UTF_8.toString())
                navController.navigate("taskDetail/$encodedTitle/$encodedDesc")
            }
        }
        composable(
            route = "taskDetail/{title}/{description}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val description = backStackEntry.arguments?.getString("description") ?: ""
            TaskDetailScreen(
                taskTitle = URLDecoder.decode(title, StandardCharsets.UTF_8.toString()),
                taskDescription = URLDecoder.decode(description, StandardCharsets.UTF_8.toString()),
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    onTaskClick: (com.ms.fieldworkreporter.domain.model.Task) -> Unit
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Home", "Settings")
    val icons = listOf(Icons.Default.Home, Icons.Default.Settings)
    
    var showAddTaskDialog by remember { mutableStateOf(value = false) }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, description ->
                homeViewModel.addTask(title, description)
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Field Work Reporter") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    if (selectedItem == 0) {
                        IconButton(onClick = { showAddTaskDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Task")
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
