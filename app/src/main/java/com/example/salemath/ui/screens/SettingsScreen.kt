package com.example.salemath.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salemath.model.AppTheme
import com.example.salemath.util.HapticHelper
import com.example.salemath.viewmodel.SaleMathViewModel
import java.math.BigDecimal

private val CURRENCY_OPTIONS = listOf("PKR", "USD", "EUR", "GBP", "AED", "SAR", "INR")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SaleMathViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val view = LocalView.current

    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Group 1: Theme & Appearance
            SettingsSectionHeader(title = "THEME & APPEARANCE")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "App Theme",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val themes = listOf(AppTheme.SYSTEM to "System", AppTheme.LIGHT to "Light", AppTheme.DARK to "Dark")
                        themes.forEachIndexed { index, (themeEnum, label) ->
                            SegmentedButton(
                                selected = settings.theme == themeEnum,
                                onClick = {
                                    viewModel.updateSettings(settings.copy(theme = themeEnum))
                                    HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = themes.size)
                            ) {
                                Text(label, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Dynamic Color", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            Text(text = "Use system wallpaper palette (Android 12+)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.dynamicColor,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(dynamicColor = it))
                                HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Group 2: Calculation Defaults
            SettingsSectionHeader(title = "CALCULATION DEFAULTS")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Default Currency", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CURRENCY_OPTIONS.forEach { cur ->
                            FilterChip(
                                selected = settings.defaultCurrency == cur,
                                onClick = {
                                    viewModel.updateSettings(settings.copy(defaultCurrency = cur))
                                    HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                                },
                                label = { Text(cur) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Decimal Places", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(6.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val places = listOf(0, 1, 2, 3, 4)
                        places.forEachIndexed { index, p ->
                            SegmentedButton(
                                selected = settings.decimalPlaces == p,
                                onClick = {
                                    viewModel.updateSettings(settings.copy(decimalPlaces = p))
                                    HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = places.size)
                            ) {
                                Text("$p", fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = settings.defaultOriginalPrice.stripTrailingZeros().toPlainString(),
                        onValueChange = {
                            val parsed = try { BigDecimal(it.replace(",", "").trim()) } catch (_: Exception) { null }
                            if (parsed != null && parsed > BigDecimal.ZERO) {
                                viewModel.updateSettings(settings.copy(defaultOriginalPrice = parsed))
                            }
                        },
                        label = { Text("Default Original Price") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = settings.defaultDiscount.stripTrailingZeros().toPlainString(),
                        onValueChange = {
                            val parsed = try { BigDecimal(it.trim()) } catch (_: Exception) { null }
                            if (parsed != null && parsed >= BigDecimal.ZERO && parsed <= BigDecimal("100")) {
                                viewModel.updateSettings(settings.copy(defaultDiscount = parsed))
                            }
                        },
                        label = { Text("Default Initial Discount (%)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Group 3: Display Toggles
            SettingsSectionHeader(title = "DISPLAY PREFERENCES")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingToggleRow(
                        title = "Show Discount Flow & Breakdown",
                        subtitle = "Display step-by-step reduction cards and timeline",
                        checked = settings.showDiscountBreakdown,
                        onCheckedChange = {
                            viewModel.updateSettings(settings.copy(showDiscountBreakdown = it))
                            HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Show Mathematical Formula",
                        subtitle = "Collapsible formula breakdown on calculator",
                        checked = settings.showFormula,
                        onCheckedChange = {
                            viewModel.updateSettings(settings.copy(showFormula = it))
                            HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Show Quantity Totals",
                        subtitle = "Always show quantity totals card",
                        checked = settings.showQuantityTotals,
                        onCheckedChange = {
                            viewModel.updateSettings(settings.copy(showQuantityTotals = it))
                            HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Show Savings Emphasis",
                        subtitle = "Highlight total amount saved",
                        checked = settings.showSavings,
                        onCheckedChange = {
                            viewModel.updateSettings(settings.copy(showSavings = it))
                            HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Group 4: Interaction & Behavior
            SettingsSectionHeader(title = "BEHAVIOR")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingToggleRow(
                        title = "Haptic Feedback",
                        subtitle = "Tactile confirmation on buttons and presets",
                        checked = settings.hapticFeedback,
                        onCheckedChange = {
                            viewModel.updateSettings(settings.copy(hapticFeedback = it))
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Confirm Deletion",
                        subtitle = "Ask before deleting items in history",
                        checked = settings.confirmDelete,
                        onCheckedChange = {
                            viewModel.updateSettings(settings.copy(confirmDelete = it))
                            HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Group 5: Data & Reset
            SettingsSectionHeader(title = "DATA & RESET")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showResetSettingsDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Settings to Defaults")
                    }

                    OutlinedButton(
                        onClick = { showClearHistoryDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear All History", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Reset settings dialog
    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Restore all application settings to their default values?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSettings()
                        HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                        showResetSettingsDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear history dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History") },
            text = { Text("Delete all saved calculations in history? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        HapticHelper.performMediumHaptic(view, settings.hapticFeedback)
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
