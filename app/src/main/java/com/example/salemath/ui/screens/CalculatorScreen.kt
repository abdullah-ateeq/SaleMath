package com.example.salemath.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salemath.model.RoundingDirection
import com.example.salemath.ui.components.DiscountBreakdownCard
import com.example.salemath.ui.components.DiscountComparisonCard
import com.example.salemath.ui.components.DiscountFlowView
import com.example.salemath.ui.components.DiscountRowItem
import com.example.salemath.ui.components.FormulaView
import com.example.salemath.ui.components.PrimaryResultCard
import com.example.salemath.ui.components.QuantityResultCard
import com.example.salemath.ui.components.RoundingInfoCard
import com.example.salemath.util.HapticHelper
import com.example.salemath.util.MoneyFormatter
import com.example.salemath.viewmodel.SaleMathViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal

private val CURRENCIES = listOf("PKR", "USD", "EUR", "GBP", "AED", "SAR", "INR", "Other")

private val PRESETS = listOf(
    "10% OFF" to listOf(BigDecimal("10")),
    "20% OFF" to listOf(BigDecimal("20")),
    "30% OFF" to listOf(BigDecimal("30")),
    "50% OFF" to listOf(BigDecimal("50")),
    "20% + 10%" to listOf(BigDecimal("20"), BigDecimal("10")),
    "20% + 10% + 5%" to listOf(BigDecimal("20"), BigDecimal("10"), BigDecimal("5"))
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalculatorScreen(
    viewModel: SaleMathViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currencyDropdownOpen by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWide = maxWidth >= 840.dp

            if (isWide) {
                // Two column layout for landscape / tablets
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(end = 16.dp, top = 16.dp, bottom = 32.dp)
                    ) {
                        CalculatorInputsSection(
                            viewModel = viewModel,
                            uiState = uiState,
                            settings = settings,
                            view = view,
                            onOpenCurrency = { currencyDropdownOpen = true },
                            currencyDropdownOpen = currencyDropdownOpen,
                            onDismissCurrency = { currencyDropdownOpen = false }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(start = 16.dp, top = 16.dp, bottom = 32.dp)
                    ) {
                        CalculatorResultsAndActions(
                            viewModel = viewModel,
                            uiState = uiState,
                            settings = settings,
                            view = view,
                            onCopyFinalPrice = {
                                val text = viewModel.getCopyFinalPriceText()
                                clipboardManager.setText(AnnotatedString(text))
                                HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Final price copied",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onCopyResult = {
                                val text = viewModel.getCopyResultText()
                                clipboardManager.setText(AnnotatedString(text))
                                HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Result copied",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onShareResult = {
                                val text = viewModel.getCopyResultText()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share SaleMath Result")
                                context.startActivity(shareIntent)
                            },
                            onSaveClick = {
                                showSaveDialog = true
                            },
                            onResetClick = {
                                HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                                viewModel.resetToDefaults()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Calculator reset to defaults")
                                }
                            }
                        )
                    }
                }
            } else {
                // Single column layout for phones
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    CalculatorInputsSection(
                        viewModel = viewModel,
                        uiState = uiState,
                        settings = settings,
                        view = view,
                        onOpenCurrency = { currencyDropdownOpen = true },
                        currencyDropdownOpen = currencyDropdownOpen,
                        onDismissCurrency = { currencyDropdownOpen = false }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    CalculatorResultsAndActions(
                        viewModel = viewModel,
                        uiState = uiState,
                        settings = settings,
                        view = view,
                        onCopyFinalPrice = {
                            val text = viewModel.getCopyFinalPriceText()
                            clipboardManager.setText(AnnotatedString(text))
                            HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Final price copied",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        onCopyResult = {
                            val text = viewModel.getCopyResultText()
                            clipboardManager.setText(AnnotatedString(text))
                            HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Result copied",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        onShareResult = {
                            val text = viewModel.getCopyResultText()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share SaleMath Result")
                            context.startActivity(shareIntent)
                        },
                        onSaveClick = {
                            showSaveDialog = true
                        },
                        onResetClick = {
                            HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                            viewModel.resetToDefaults()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Calculator reset to defaults")
                            }
                        }
                    )
                }
            }
        }
    }

    // Save Dialog
    if (showSaveDialog) {
        var tempName by remember { mutableStateOf(uiState.projectName) }
        var tempNotes by remember { mutableStateOf(uiState.notes) }

        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Calculation") },
            text = {
                Column {
                    Text(
                        text = "Store this sequential discount calculation in your offline history.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { if (it.length <= 80) tempName = it },
                        label = { Text("Project / Item Name") },
                        placeholder = { Text("e.g. Dining Chair, Customer Quote") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempNotes,
                        onValueChange = { if (it.length <= 500) tempNotes = it },
                        label = { Text("Notes (optional)") },
                        placeholder = { Text("e.g. Weekend promotion, bulk sale") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onProjectNameChange(tempName)
                        viewModel.onNotesChange(tempNotes)
                        viewModel.saveCurrentCalculation {
                            HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Calculation saved to History")
                            }
                        }
                        showSaveDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalculatorInputsSection(
    viewModel: SaleMathViewModel,
    uiState: com.example.salemath.viewmodel.CalculatorUiState,
    settings: com.example.salemath.model.AppSettings,
    view: android.view.View?,
    onOpenCurrency: () -> Unit,
    currencyDropdownOpen: Boolean,
    onDismissCurrency: () -> Unit
) {
    // Header
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SALEMATH",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Calculate sale price after multiple discounts.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Project Name & Currency
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = uiState.projectName,
            onValueChange = { viewModel.onProjectNameChange(it) },
            label = { Text("Project / Item (optional)") },
            placeholder = { Text("Untitled Sale") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        Box {
            OutlinedButton(
                onClick = onOpenCurrency,
                modifier = Modifier.height(56.dp)
            ) {
                Text(uiState.currency, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select currency")
            }

            DropdownMenu(
                expanded = currencyDropdownOpen,
                onDismissRequest = onDismissCurrency
            ) {
                CURRENCIES.forEach { cur ->
                    DropdownMenuItem(
                        text = { Text(cur) },
                        onClick = {
                            viewModel.onCurrencyChange(cur)
                            onDismissCurrency()
                        }
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Original Price
    OutlinedTextField(
        value = uiState.originalPriceText,
        onValueChange = { viewModel.onOriginalPriceChange(it) },
        label = { Text("Original Price") },
        prefix = { Text(MoneyFormatter.getCurrencyPrefix(uiState.currency), fontWeight = FontWeight.SemiBold) },
        singleLine = true,
        isError = uiState.originalPriceError != null,
        supportingText = {
            if (uiState.originalPriceError != null) {
                Text(uiState.originalPriceError!!, color = MaterialTheme.colorScheme.error)
            } else {
                Text("Price before applying any discounts")
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Original price ${uiState.originalPriceText}" }
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Quantity Input & Stepper
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "QUANTITY",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Calculates per-unit first, then multiplies total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(
                onClick = {
                    if (uiState.quantity > 1) {
                        viewModel.onQuantityChange(uiState.quantity - 1)
                    }
                },
                enabled = uiState.quantity > 1,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "${uiState.quantity}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            FilledTonalButton(
                onClick = {
                    if (uiState.quantity < 1_000_000L) {
                        viewModel.onQuantityChange(uiState.quantity + 1)
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Quick Presets
    Text(
        text = "DISCOUNT PRESETS",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(6.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PRESETS.forEach { (label, percents) ->
            FilterChip(
                selected = false,
                onClick = {
                    HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                    viewModel.applyPreset(percents)
                },
                label = { Text(label, fontSize = 12.sp) }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Discounts Section Header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SEQUENTIAL DISCOUNTS (${uiState.discounts.size}/10)",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.primary
        )

        Button(
            onClick = {
                HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                viewModel.addDiscount()
            },
            enabled = uiState.discounts.size < 10,
            modifier = Modifier.semantics { contentDescription = "Add discount" }
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("ADD DISCOUNT")
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Discount Rows
    uiState.discounts.forEachIndexed { index, row ->
        DiscountRowItem(
            row = row,
            index = index,
            totalCount = uiState.discounts.size,
            onValueChange = { viewModel.updateDiscount(row.id, it) },
            onQuickSelect = {
                HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                viewModel.setDiscountQuickValue(row.id, it)
            },
            onRemove = {
                HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                viewModel.removeDiscount(row.id)
            },
            onMoveUp = {
                HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                viewModel.moveDiscountUp(index)
            },
            onMoveDown = {
                HapticHelper.performLightHaptic(view, settings.hapticFeedback)
                viewModel.moveDiscountDown(index)
            },
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Optional Final Price Rounding
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ROUND FINAL PRICE",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Optional retail rounding (e.g. nearest 5 or 50)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.roundingEnabled,
                    onCheckedChange = { viewModel.onRoundingEnabledChange(it) }
                )
            }

            if (uiState.roundingEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Rounding Increment:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val increments = listOf("1", "5", "10", "50", "100")
                    increments.forEach { inc ->
                        val incBd = BigDecimal(inc)
                        val isSelected = uiState.roundingIncrement.compareTo(incBd) == 0
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onRoundingIncrementChange(incBd) },
                            label = { Text("Nearest $inc") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rounding Direction:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = uiState.roundingDirection == RoundingDirection.NEAREST,
                        onClick = { viewModel.onRoundingDirectionChange(RoundingDirection.NEAREST) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) {
                        Text("Nearest", fontSize = 12.sp)
                    }
                    SegmentedButton(
                        selected = uiState.roundingDirection == RoundingDirection.UP,
                        onClick = { viewModel.onRoundingDirectionChange(RoundingDirection.UP) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) {
                        Text("Up", fontSize = 12.sp)
                    }
                    SegmentedButton(
                        selected = uiState.roundingDirection == RoundingDirection.DOWN,
                        onClick = { viewModel.onRoundingDirectionChange(RoundingDirection.DOWN) },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) {
                        Text("Down", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorResultsAndActions(
    viewModel: SaleMathViewModel,
    uiState: com.example.salemath.viewmodel.CalculatorUiState,
    settings: com.example.salemath.model.AppSettings,
    view: android.view.View?,
    onCopyFinalPrice: () -> Unit,
    onCopyResult: () -> Unit,
    onShareResult: () -> Unit,
    onSaveClick: () -> Unit,
    onResetClick: () -> Unit
) {
    val result = uiState.calculationResult

    if (result != null) {
        // 1. Primary Result Card (Visually dominant)
        PrimaryResultCard(
            result = result,
            currency = uiState.currency,
            decimalPlaces = settings.decimalPlaces
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Optional Rounding card if enabled
        if (uiState.roundingEnabled) {
            RoundingInfoCard(
                result = result,
                currency = uiState.currency,
                decimalPlaces = settings.decimalPlaces
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. Discount Comparison Card
        DiscountComparisonCard(
            result = result,
            decimalPlaces = settings.decimalPlaces
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Quantity Totals Card (if quantity > 1 or setting enabled)
        if (uiState.quantity > 1 || settings.showQuantityTotals) {
            QuantityResultCard(
                result = result,
                currency = uiState.currency,
                decimalPlaces = settings.decimalPlaces
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 4. Discount Flow Visualization (Timeline)
        if (settings.showDiscountBreakdown) {
            DiscountFlowView(
                result = result,
                currency = uiState.currency,
                decimalPlaces = settings.decimalPlaces
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 5. Step-by-Step Breakdown Card
            DiscountBreakdownCard(
                result = result,
                currency = uiState.currency,
                decimalPlaces = settings.decimalPlaces
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 6. Formula Section (Collapsible)
        if (settings.showFormula) {
            FormulaView(
                result = result,
                decimalPlaces = settings.decimalPlaces
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCopyFinalPrice,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Copy final price" }
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Price", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onCopyResult,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Copy result" }
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Result", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onShareResult,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Share result" }
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Save calculation" }
            ) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save to History")
            }

            OutlinedButton(
                onClick = onResetClick,
                modifier = Modifier.semantics { contentDescription = "Reset calculator" }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset")
            }
        }
    } else {
        // Fallback placeholder when original price is invalid or 0
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter a valid original price to calculate discounts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Disclaimer
    Text(
        text = "SaleMath performs mathematical discount calculations only. It does not determine whether a discount, sale price, or promotion is commercially appropriate.",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}
