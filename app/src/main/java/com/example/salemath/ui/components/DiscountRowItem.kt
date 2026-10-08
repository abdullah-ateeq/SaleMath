package com.example.salemath.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salemath.viewmodel.DiscountRowState
import java.math.BigDecimal

private val QUICK_DISCOUNTS = listOf(
    BigDecimal("5"),
    BigDecimal("10"),
    BigDecimal("15"),
    BigDecimal("20"),
    BigDecimal("25"),
    BigDecimal("30"),
    BigDecimal("50")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscountRowItem(
    row: DiscountRowState,
    index: Int,
    totalCount: Int,
    onValueChange: (String) -> Unit,
    onQuickSelect: (BigDecimal) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DISCOUNT ${index + 1}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (totalCount > 1) {
                        if (index > 0) {
                            IconButton(
                                onClick = onMoveUp,
                                modifier = Modifier
                                    .size(32.dp)
                                    .semantics { contentDescription = "Move discount up" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (index < totalCount - 1) {
                            IconButton(
                                onClick = onMoveDown,
                                modifier = Modifier
                                    .size(32.dp)
                                    .semantics { contentDescription = "Move discount down" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onRemove,
                        enabled = totalCount > 1,
                        modifier = Modifier
                            .size(32.dp)
                            .semantics { contentDescription = "Remove discount" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = if (totalCount > 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = row.percentText,
                onValueChange = onValueChange,
                label = { Text("Percentage") },
                suffix = { Text("%", fontWeight = FontWeight.SemiBold) },
                singleLine = true,
                isError = row.errorMessage != null,
                supportingText = {
                    when {
                        row.errorMessage != null -> {
                            Text(text = row.errorMessage, color = MaterialTheme.colorScheme.error)
                        }
                        row.percentText.trim() == "0" -> {
                            Text(text = "No reduction", color = MaterialTheme.colorScheme.secondary)
                        }
                        else -> {
                            Text(text = "Applied sequentially to remaining price")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "${row.percentText} percent discount"
                    }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Quick discount chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                QUICK_DISCOUNTS.forEach { quickVal ->
                    val quickValStr = quickVal.stripTrailingZeros().toPlainString()
                    val isSelected = row.percentText.trim() == quickValStr
                    FilterChip(
                        selected = isSelected,
                        onClick = { onQuickSelect(quickVal) },
                        label = { Text("${quickValStr}%", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                }
            }
        }
    }
}
