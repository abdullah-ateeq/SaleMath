package com.example.salemath.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Splash : Screen("splash", "Splash")
    data object Calculator : Screen("calculator", "Calculator", Icons.Outlined.Calculate)
    data object History : Screen("history", "History", Icons.Outlined.History)
    data object Settings : Screen("settings", "Settings", Icons.Outlined.Settings)
    data object About : Screen("about", "About", Icons.Outlined.Info)

    companion object {
        val bottomNavItems = listOf(Calculator, History, Settings, About)
    }
}
