package com.example.salemath.model

import java.math.BigDecimal

data class SaleMathResult(
    val originalPrice: BigDecimal,
    val calculatedFinalPrice: BigDecimal,
    val actualFinalPrice: BigDecimal,
    val totalSavings: BigDecimal,
    val effectiveDiscountPercent: BigDecimal,
    val steps: List<DiscountStep>,
    val quantity: Long,
    val totalOriginal: BigDecimal,
    val totalFinal: BigDecimal,
    val totalSavingsAmount: BigDecimal
)
