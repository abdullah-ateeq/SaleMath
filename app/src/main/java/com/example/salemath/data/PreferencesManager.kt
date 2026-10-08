package com.example.salemath.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.salemath.model.AppSettings
import com.example.salemath.model.AppTheme
import com.example.salemath.model.RoundingDirection
import com.example.salemath.model.SavedCalculation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "salemath_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        private const val MAX_SAVED_CALCULATIONS = 50

        private val KEY_SAVED_CALCULATIONS = stringPreferencesKey("saved_calculations")
        private val KEY_THEME = stringPreferencesKey("app_theme")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        private val KEY_DEFAULT_ORIGINAL_PRICE = stringPreferencesKey("default_original_price")
        private val KEY_DEFAULT_QUANTITY = longPreferencesKey("default_quantity")
        private val KEY_DEFAULT_DISCOUNT = stringPreferencesKey("default_discount")
        private val KEY_DEFAULT_DISCOUNT_PRESET = stringPreferencesKey("default_discount_preset")
        private val KEY_DEFAULT_FINAL_ROUNDING = booleanPreferencesKey("default_final_rounding")
        private val KEY_DEFAULT_ROUNDING_INCREMENT = stringPreferencesKey("default_rounding_increment")
        private val KEY_DEFAULT_ROUNDING_DIRECTION = stringPreferencesKey("default_rounding_direction")
        private val KEY_DECIMAL_PLACES = intPreferencesKey("decimal_places")
        private val KEY_SHOW_DISCOUNT_BREAKDOWN = booleanPreferencesKey("show_discount_breakdown")
        private val KEY_SHOW_FORMULA = booleanPreferencesKey("show_formula")
        private val KEY_SHOW_QUANTITY_TOTALS = booleanPreferencesKey("show_quantity_totals")
        private val KEY_SHOW_SAVINGS = booleanPreferencesKey("show_savings")
        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        private val KEY_CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[KEY_THEME] ?: AppTheme.SYSTEM.name
        val theme = try {
            AppTheme.valueOf(themeStr)
        } catch (_: Exception) {
            AppTheme.SYSTEM
        }

        val roundingDirStr = prefs[KEY_DEFAULT_ROUNDING_DIRECTION] ?: RoundingDirection.NEAREST.name
        val roundingDir = try {
            RoundingDirection.valueOf(roundingDirStr)
        } catch (_: Exception) {
            RoundingDirection.NEAREST
        }

        val originalPriceStr = prefs[KEY_DEFAULT_ORIGINAL_PRICE] ?: "10000"
        val origPrice = try { BigDecimal(originalPriceStr) } catch (_: Exception) { BigDecimal("10000") }

        val discountStr = prefs[KEY_DEFAULT_DISCOUNT] ?: "20"
        val discount = try { BigDecimal(discountStr) } catch (_: Exception) { BigDecimal("20") }

        val incrementStr = prefs[KEY_DEFAULT_ROUNDING_INCREMENT] ?: "1"
        val increment = try { BigDecimal(incrementStr) } catch (_: Exception) { BigDecimal("1") }

        AppSettings(
            theme = theme,
            dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: false,
            defaultCurrency = prefs[KEY_DEFAULT_CURRENCY] ?: "PKR",
            defaultOriginalPrice = origPrice,
            defaultQuantity = prefs[KEY_DEFAULT_QUANTITY] ?: 1L,
            defaultDiscount = discount,
            defaultDiscountPreset = prefs[KEY_DEFAULT_DISCOUNT_PRESET] ?: "None",
            defaultFinalRounding = prefs[KEY_DEFAULT_FINAL_ROUNDING] ?: false,
            defaultRoundingIncrement = increment,
            defaultRoundingDirection = roundingDir,
            decimalPlaces = prefs[KEY_DECIMAL_PLACES] ?: 2,
            showDiscountBreakdown = prefs[KEY_SHOW_DISCOUNT_BREAKDOWN] ?: true,
            showFormula = prefs[KEY_SHOW_FORMULA] ?: true,
            showQuantityTotals = prefs[KEY_SHOW_QUANTITY_TOTALS] ?: true,
            showSavings = prefs[KEY_SHOW_SAVINGS] ?: true,
            hapticFeedback = prefs[KEY_HAPTIC_FEEDBACK] ?: true,
            confirmDelete = prefs[KEY_CONFIRM_DELETE] ?: true
        )
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = settings.theme.name
            prefs[KEY_DYNAMIC_COLOR] = settings.dynamicColor
            prefs[KEY_DEFAULT_CURRENCY] = settings.defaultCurrency
            prefs[KEY_DEFAULT_ORIGINAL_PRICE] = settings.defaultOriginalPrice.toPlainString()
            prefs[KEY_DEFAULT_QUANTITY] = settings.defaultQuantity
            prefs[KEY_DEFAULT_DISCOUNT] = settings.defaultDiscount.toPlainString()
            prefs[KEY_DEFAULT_DISCOUNT_PRESET] = settings.defaultDiscountPreset
            prefs[KEY_DEFAULT_FINAL_ROUNDING] = settings.defaultFinalRounding
            prefs[KEY_DEFAULT_ROUNDING_INCREMENT] = settings.defaultRoundingIncrement.toPlainString()
            prefs[KEY_DEFAULT_ROUNDING_DIRECTION] = settings.defaultRoundingDirection.name
            prefs[KEY_DECIMAL_PLACES] = settings.decimalPlaces
            prefs[KEY_SHOW_DISCOUNT_BREAKDOWN] = settings.showDiscountBreakdown
            prefs[KEY_SHOW_FORMULA] = settings.showFormula
            prefs[KEY_SHOW_QUANTITY_TOTALS] = settings.showQuantityTotals
            prefs[KEY_SHOW_SAVINGS] = settings.showSavings
            prefs[KEY_HAPTIC_FEEDBACK] = settings.hapticFeedback
            prefs[KEY_CONFIRM_DELETE] = settings.confirmDelete
        }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_THEME)
            prefs.remove(KEY_DYNAMIC_COLOR)
            prefs.remove(KEY_DEFAULT_CURRENCY)
            prefs.remove(KEY_DEFAULT_ORIGINAL_PRICE)
            prefs.remove(KEY_DEFAULT_QUANTITY)
            prefs.remove(KEY_DEFAULT_DISCOUNT)
            prefs.remove(KEY_DEFAULT_DISCOUNT_PRESET)
            prefs.remove(KEY_DEFAULT_FINAL_ROUNDING)
            prefs.remove(KEY_DEFAULT_ROUNDING_INCREMENT)
            prefs.remove(KEY_DEFAULT_ROUNDING_DIRECTION)
            prefs.remove(KEY_DECIMAL_PLACES)
            prefs.remove(KEY_SHOW_DISCOUNT_BREAKDOWN)
            prefs.remove(KEY_SHOW_FORMULA)
            prefs.remove(KEY_SHOW_QUANTITY_TOTALS)
            prefs.remove(KEY_SHOW_SAVINGS)
            prefs.remove(KEY_HAPTIC_FEEDBACK)
            prefs.remove(KEY_CONFIRM_DELETE)
        }
    }

    val historyFlow: Flow<List<SavedCalculation>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_SAVED_CALCULATIONS] ?: "[]"
        SavedCalculationJson.fromJson(json)
    }

    suspend fun saveCalculation(calculation: SavedCalculation) {
        context.dataStore.edit { prefs ->
            val json = prefs[KEY_SAVED_CALCULATIONS] ?: "[]"
            val currentList = SavedCalculationJson.fromJson(json).toMutableList()

            // Remove existing if updating same ID
            currentList.removeAll { it.id == calculation.id }

            // Add new calculation to front (most recent)
            currentList.add(0, calculation)

            // Keep only max 50 items (oldest removed on #51)
            while (currentList.size > MAX_SAVED_CALCULATIONS) {
                currentList.removeAt(currentList.lastIndex)
            }

            prefs[KEY_SAVED_CALCULATIONS] = SavedCalculationJson.toJson(currentList)
        }
    }

    suspend fun deleteCalculation(id: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[KEY_SAVED_CALCULATIONS] ?: "[]"
            val currentList = SavedCalculationJson.fromJson(json).toMutableList()
            currentList.removeAll { it.id == id }
            prefs[KEY_SAVED_CALCULATIONS] = SavedCalculationJson.toJson(currentList)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { prefs ->
            prefs[KEY_SAVED_CALCULATIONS] = "[]"
        }
    }

    suspend fun toggleFavorite(id: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[KEY_SAVED_CALCULATIONS] ?: "[]"
            val currentList = SavedCalculationJson.fromJson(json).toMutableList()
            val index = currentList.indexOfFirst { it.id == id }
            if (index != -1) {
                val item = currentList[index]
                currentList[index] = item.copy(isFavorite = !item.isFavorite)
                prefs[KEY_SAVED_CALCULATIONS] = SavedCalculationJson.toJson(currentList)
            }
        }
    }

    suspend fun duplicateCalculation(id: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[KEY_SAVED_CALCULATIONS] ?: "[]"
            val currentList = SavedCalculationJson.fromJson(json).toMutableList()
            val original = currentList.find { it.id == id }
            if (original != null) {
                val copyName = if (original.projectName.isBlank()) "Copy of Untitled Sale" else "Copy of ${original.projectName}"
                val duplicated = original.copy(
                    id = UUID.randomUUID().toString(),
                    projectName = copyName,
                    timestamp = System.currentTimeMillis(),
                    isFavorite = false
                )
                currentList.add(0, duplicated)
                while (currentList.size > MAX_SAVED_CALCULATIONS) {
                    currentList.removeAt(currentList.lastIndex)
                }
                prefs[KEY_SAVED_CALCULATIONS] = SavedCalculationJson.toJson(currentList)
            }
        }
    }
}
