package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.compatibility.CompatibilityScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.games.GamesScreen
import com.example.ui.screens.monitor.MonitorScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.FpsBoosterTheme
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextGray
import com.example.viewmodel.MainViewModel

data class NavItem(val title: String, val icon: ImageVector, val tag: String)

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FpsBoosterTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navItems = listOf(
        NavItem("Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
        NavItem("Games", Icons.Default.SportsEsports, "nav_games"),
        NavItem("Monitor", Icons.Default.Speed, "nav_monitor"),
        NavItem("Diagnostics", Icons.Default.Assessment, "nav_diagnostics"),
        NavItem("Settings", Icons.Default.Settings, "nav_settings")
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(1.dp, CardBorder)
                    .testTag("main_navigation_bar")
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonYellow,
                            selectedTextColor = NeonYellow,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray,
                            indicatorColor = DarkSurface
                        ),
                        modifier = Modifier.testTag(item.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (selectedIndex) {
            0 -> DashboardScreen(
                viewModel = viewModel,
                onNavigateToSettings = { selectedIndex = 4 },
                onNavigateToGames = { selectedIndex = 1 },
                modifier = contentModifier
            )
            1 -> GamesScreen(
                viewModel = viewModel,
                modifier = contentModifier
            )
            2 -> MonitorScreen(
                viewModel = viewModel,
                modifier = contentModifier
            )
            3 -> CompatibilityScreen(
                viewModel = viewModel,
                modifier = contentModifier
            )
            4 -> SettingsScreen(
                viewModel = viewModel,
                modifier = contentModifier
            )
        }
    }
}
