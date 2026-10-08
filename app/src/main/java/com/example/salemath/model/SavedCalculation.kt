package com.example.salemath.model

import java.math.BigDecimal

data class SavedCalculation(
    val id: String,
    val projectName: String,
    val notes: String,
    val currency: String,
    val originalPrice: BigDecimal,
    val discounts: List<BigDecimal>,
    val quantity: Long,
    val calculatedFinalPrice: BigDecimal,
    val actualFinalPrice: BigDecimal,
    val totalSavings: BigDecimal,
    val effectiveDiscountPercent: BigDecimal,
    val timestamp: Long,
    val isFavorite: Boolean = false,
    val roundingEnabled: Boolean = false,
    val roundingIncrement: BigDecimal? = null,
    val roundingDirection: RoundingDirection = RoundingDirection.NEAREST
)
