package com.example.salemath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.salemath.calculator.SaleMathCalculator
import com.example.salemath.data.PreferencesManager
import com.example.salemath.model.AppSettings
import com.example.salemath.model.DiscountEntry
import com.example.salemath.model.RoundingDirection
import com.example.salemath.model.SaleMathInput
import com.example.salemath.model.SaleMathResult
import com.example.salemath.model.SavedCalculation
import com.example.salemath.util.MoneyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID

enum class HistoryFilter {
    ALL,
    ONE_DISCOUNT,
    MULTIPLE_DISCOUNTS
}

data class DiscountRowState(
    val id: String = UUID.randomUUID().toString(),
    val percentText: String = "",
    val errorMessage: String? = null
)

data class CalculatorUiState(
    val projectName: String = "",
    val notes: String = "",
    val currency: String = "PKR",
    val originalPriceText: String = "10000",
    val originalPriceError: String? = null,
    val discounts: List<DiscountRowState> = listOf(
        DiscountRowState(id = UUID.randomUUID().toString(), percentText = "20")
    ),
    val quantity: Long = 1L,
    val roundingEnabled: Boolean = false,
    val roundingIncrement: BigDecimal = BigDecimal("1"),
    val customIncrementText: String = "1",
    val roundingDirection: RoundingDirection = RoundingDirection.NEAREST,
    val calculationResult: SaleMathResult? = null
)

class SaleMathViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)

    val settings: StateFlow<AppSettings> = preferencesManager.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettings()
    )

    val allHistory: StateFlow<List<SavedCalculation>> = preferencesManager.historyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val historyFilter: StateFlow<HistoryFilter> = _historyFilter.asStateFlow()

    // Filtered and sorted history: favorites first, search query match, filter match
    val filteredHistory: StateFlow<List<SavedCalculation>> = combine(
        allHistory,
        _historySearchQuery,
        _historyFilter
    ) { history, query, filter ->
        val trimmedQuery = query.trim().lowercase()
        history.filter { item ->
            val matchesQuery = if (trimmedQuery.isEmpty()) {
                true
            } else {
                item.projectName.lowercase().contains(trimmedQuery) ||
                        item.notes.lowercase().contains(trimmedQuery)
            }

            val matchesFilter = when (filter) {
                HistoryFilter.ALL -> true
                HistoryFilter.ONE_DISCOUNT -> item.discounts.size == 1
                HistoryFilter.MULTIPLE_DISCOUNTS -> item.discounts.size > 1
            }

            matchesQuery && matchesFilter
        }.sortedWith(
            compareByDescending<SavedCalculation> { it.isFavorite }
                .thenByDescending { it.timestamp }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        // Initialize with default calculation
        recalculate()
    }

    fun onProjectNameChange(name: String) {
        val trimmed = if (name.length > 80) name.substring(0, 80) else name
        _uiState.value = _uiState.value.copy(projectName = trimmed)
    }

    fun onNotesChange(notes: String) {
        val trimmed = if (notes.length > 500) notes.substring(0, 500) else notes
        _uiState.value = _uiState.value.copy(notes = trimmed)
    }

    fun onCurrencyChange(currency: String) {
        _uiState.value = _uiState.value.copy(currency = currency)
        recalculate()
    }

    fun onOriginalPriceChange(priceText: String) {
        val error = validateOriginalPrice(priceText)
        _uiState.value = _uiState.value.copy(
            originalPriceText = priceText,
            originalPriceError = error
        )
        recalculate()
    }

    private fun validateOriginalPrice(text: String): String? {
        val cleaned = text.trim().replace(",", "")
        if (cleaned.isEmpty()) return null
        val parsed = try { BigDecimal(cleaned) } catch (_: Exception) { return "Invalid price format." }
        if (parsed <= BigDecimal.ZERO) return "Original price must be greater than zero."
        if (parsed > BigDecimal("999999999999.99")) return "Maximum original price is 999,999,999,999.99."
        return null
    }

    private fun validateDiscount(text: String): String? {
        val cleaned = text.trim()
        if (cleaned.isEmpty()) return null
        val parsed = try { BigDecimal(cleaned) } catch (_: Exception) { return "Invalid discount format." }
        if (parsed < BigDecimal.ZERO || parsed > BigDecimal("100")) return "Discount must be between 0% and 100%."
        return null
    }

    fun addDiscount(initialPercent: BigDecimal = BigDecimal("10")): Boolean {
        val currentDiscounts = _uiState.value.discounts
        if (currentDiscounts.size >= 10) return false

        val newDiscount = DiscountRowState(
            id = UUID.randomUUID().toString(),
            percentText = initialPercent.stripTrailingZeros().toPlainString()
        )
        _uiState.value = _uiState.value.copy(discounts = currentDiscounts + newDiscount)
        recalculate()
        return true
    }

    fun removeDiscount(id: String): Boolean {
        val currentDiscounts = _uiState.value.discounts
        if (currentDiscounts.size <= 1) return false

        _uiState.value = _uiState.value.copy(
            discounts = currentDiscounts.filter { it.id != id }
        )
        recalculate()
        return true
    }

    fun updateDiscount(id: String, percentText: String) {
        val error = validateDiscount(percentText)
        val updated = _uiState.value.discounts.map { row ->
            if (row.id == id) {
                row.copy(percentText = percentText, errorMessage = error)
            } else {
                row
            }
        }
        _uiState.value = _uiState.value.copy(discounts = updated)
        recalculate()
    }

    fun setDiscountQuickValue(id: String, percent: BigDecimal) {
        val formatted = percent.stripTrailingZeros().toPlainString()
        updateDiscount(id, formatted)
    }

    fun moveDiscountUp(index: Int) {
        if (index <= 0) return
        val current = _uiState.value.discounts.toMutableList()
        val temp = current[index]
        current[index] = current[index - 1]
        current[index - 1] = temp
        _uiState.value = _uiState.value.copy(discounts = current)
        recalculate()
    }

    fun moveDiscountDown(index: Int) {
        val current = _uiState.value.discounts.toMutableList()
        if (index >= current.size - 1) return
        val temp = current[index]
        current[index] = current[index + 1]
        current[index + 1] = temp
        _uiState.value = _uiState.value.copy(discounts = current)
        recalculate()
    }

    fun applyPreset(presetPercents: List<BigDecimal>) {
        if (presetPercents.isEmpty()) return
        val clamped = presetPercents.take(10)
        val newDiscounts = clamped.map { percent ->
            DiscountRowState(
                id = UUID.randomUUID().toString(),
                percentText = percent.stripTrailingZeros().toPlainString()
            )
        }
        _uiState.value = _uiState.value.copy(discounts = newDiscounts)
        recalculate()
    }

    fun onQuantityChange(newQuantity: Long) {
        val clamped = newQuantity.coerceIn(1L, 1_000_000L)
        _uiState.value = _uiState.value.copy(quantity = clamped)
        recalculate()
    }

    fun onRoundingEnabledChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(roundingEnabled = enabled)
        recalculate()
    }

    fun onRoundingIncrementChange(increment: BigDecimal) {
        _uiState.value = _uiState.value.copy(
            roundingIncrement = increment,
            customIncrementText = increment.toPlainString()
        )
        recalculate()
    }

    fun onCustomIncrementChange(text: String) {
        val parsed = MoneyFormatter.parseBigDecimalOrNull(text)
        val inc = if (parsed != null && parsed > BigDecimal.ZERO) parsed else BigDecimal("1")
        _uiState.value = _uiState.value.copy(
            customIncrementText = text,
            roundingIncrement = inc
        )
        recalculate()
    }

    fun onRoundingDirectionChange(direction: RoundingDirection) {
        _uiState.value = _uiState.value.copy(roundingDirection = direction)
        recalculate()
    }

    private fun recalculate() {
        val state = _uiState.value
        val origPriceParsed = MoneyFormatter.parseBigDecimalOrNull(state.originalPriceText)
        if (origPriceParsed == null || origPriceParsed <= BigDecimal.ZERO) {
            _uiState.value = state.copy(calculationResult = null)
            return
        }

        val discountEntries = state.discounts.map { row ->
            val p = MoneyFormatter.parseBigDecimalOrNull(row.percentText) ?: BigDecimal.ZERO
            DiscountEntry(id = row.id, percent = p)
        }

        val input = SaleMathInput(
            projectName = state.projectName,
            notes = state.notes,
            currency = state.currency,
            originalPrice = origPriceParsed,
            discounts = discountEntries,
            quantity = state.quantity,
            roundingEnabled = state.roundingEnabled,
            roundingIncrement = state.roundingIncrement,
            roundingDirection = state.roundingDirection
        )

        val result = SaleMathCalculator.calculateResult(input)
        _uiState.value = state.copy(calculationResult = result)
    }

    fun resetToDefaults() {
        val s = settings.value
        val defaultDiscountRow = DiscountRowState(
            id = UUID.randomUUID().toString(),
            percentText = s.defaultDiscount.stripTrailingZeros().toPlainString()
        )
        _uiState.value = CalculatorUiState(
            projectName = "",
            notes = "",
            currency = s.defaultCurrency,
            originalPriceText = s.defaultOriginalPrice.stripTrailingZeros().toPlainString(),
            originalPriceError = null,
            discounts = listOf(defaultDiscountRow),
            quantity = s.defaultQuantity,
            roundingEnabled = s.defaultFinalRounding,
            roundingIncrement = s.defaultRoundingIncrement,
            customIncrementText = s.defaultRoundingIncrement.toPlainString(),
            roundingDirection = s.defaultRoundingDirection
        )
        recalculate()
    }

    fun saveCurrentCalculation(onSaved: (() -> Unit)? = null) {
        val result = _uiState.value.calculationResult ?: return
        val state = _uiState.value
        val projName = if (state.projectName.isBlank()) "Untitled Sale" else state.projectName

        val discountValues = state.discounts.map {
            MoneyFormatter.parseBigDecimalOrNull(it.percentText) ?: BigDecimal.ZERO
        }

        val saved = SavedCalculation(
            id = UUID.randomUUID().toString(),
            projectName = projName,
            notes = state.notes,
            currency = state.currency,
            originalPrice = result.originalPrice,
            discounts = discountValues,
            quantity = state.quantity,
            calculatedFinalPrice = result.calculatedFinalPrice,
            actualFinalPrice = result.actualFinalPrice,
            totalSavings = result.totalSavings,
            effectiveDiscountPercent = result.effectiveDiscountPercent,
            timestamp = System.currentTimeMillis(),
            isFavorite = false,
            roundingEnabled = state.roundingEnabled,
            roundingIncrement = state.roundingIncrement,
            roundingDirection = state.roundingDirection
        )

        viewModelScope.launch {
            preferencesManager.saveCalculation(saved)
            onSaved?.invoke()
        }
    }

    fun restoreCalculation(saved: SavedCalculation) {
        val restoredDiscounts = if (saved.discounts.isEmpty()) {
            listOf(DiscountRowState(percentText = "20"))
        } else {
            saved.discounts.map { d ->
                DiscountRowState(
                    id = UUID.randomUUID().toString(),
                    percentText = d.stripTrailingZeros().toPlainString()
                )
            }
        }

        _uiState.value = _uiState.value.copy(
            projectName = saved.projectName,
            notes = saved.notes,
            currency = saved.currency,
            originalPriceText = saved.originalPrice.stripTrailingZeros().toPlainString(),
            originalPriceError = null,
            discounts = restoredDiscounts,
            quantity = saved.quantity,
            roundingEnabled = saved.roundingEnabled,
            roundingIncrement = saved.roundingIncrement ?: BigDecimal("1"),
            customIncrementText = (saved.roundingIncrement ?: BigDecimal("1")).toPlainString(),
            roundingDirection = saved.roundingDirection
        )
        recalculate()
    }

    fun deleteCalculation(id: String) {
        viewModelScope.launch {
            preferencesManager.deleteCalculation(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            preferencesManager.clearHistory()
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            preferencesManager.toggleFavorite(id)
        }
    }

    fun duplicateCalculation(id: String) {
        viewModelScope.launch {
            preferencesManager.duplicateCalculation(id)
        }
    }

    fun onHistorySearchChange(query: String) {
        _historySearchQuery.value = query
    }

    fun onHistoryFilterChange(filter: HistoryFilter) {
        _historyFilter.value = filter
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            preferencesManager.updateSettings(newSettings)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            preferencesManager.resetSettings()
        }
    }

    fun getCopyFinalPriceText(): String {
        val result = _uiState.value.calculationResult ?: return ""
        val decimals = settings.value.decimalPlaces
        return MoneyFormatter.formatMoney(result.actualFinalPrice, _uiState.value.currency, decimals)
    }

    fun getCopyResultText(): String {
        val result = _uiState.value.calculationResult ?: return ""
        val state = _uiState.value
        val decimals = settings.value.decimalPlaces
        val cur = state.currency

        val discountsFormatted = state.discounts.joinToString("\n") {
            val p = MoneyFormatter.parseBigDecimalOrNull(it.percentText) ?: BigDecimal.ZERO
            "${p.stripTrailingZeros().toPlainString()}%"
        }

        val projectLine = if (state.projectName.isNotBlank()) "Project: ${state.projectName}\n" else ""
        val notesLine = if (state.notes.isNotBlank()) "Notes: ${state.notes}\n" else ""
        val quantityLine = if (state.quantity > 1) "Quantity: ${state.quantity}\nTotal Sale Value: ${MoneyFormatter.formatMoney(result.totalFinal, cur, decimals)}\n" else ""

        return """
            SaleMath
            ${projectLine}${notesLine}Original Price: ${MoneyFormatter.formatMoney(result.originalPrice, cur, decimals)}

            Discounts:
            $discountsFormatted

            Final Price: ${MoneyFormatter.formatMoney(result.actualFinalPrice, cur, decimals)}
            Total Savings: ${MoneyFormatter.formatMoney(result.totalSavings, cur, decimals)}
            Effective Discount: ${MoneyFormatter.formatPercentage(result.effectiveDiscountPercent, decimals)}
            $quantityLine
        """.trimIndent()
    }
}
