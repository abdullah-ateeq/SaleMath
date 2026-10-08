package com.example.salemath.model

import java.math.BigDecimal

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = false,
    val defaultCurrency: String = "PKR",
    val defaultOriginalPrice: BigDecimal = BigDecimal("10000"),
    val defaultQuantity: Long = 1L,
    val defaultDiscount: BigDecimal = BigDecimal("20"),
    val defaultDiscountPreset: String = "None",
    val defaultFinalRounding: Boolean = false,
    val defaultRoundingIncrement: BigDecimal = BigDecimal("1"),
    val defaultRoundingDirection: RoundingDirection = RoundingDirection.NEAREST,
    val decimalPlaces: Int = 2,
    val showDiscountBreakdown: Boolean = true,
    val showFormula: Boolean = true,
    val showQuantityTotals: Boolean = true,
    val showSavings: Boolean = true,
    val hapticFeedback: Boolean = true,
    val confirmDelete: Boolean = true
)
