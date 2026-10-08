package com.example.salemath.model

import java.math.BigDecimal

data class DiscountStep(
    val stepNumber: Int,
    val discountPercent: BigDecimal,
    val priceBefore: BigDecimal,
    val discountAmount: BigDecimal,
    val priceAfter: BigDecimal
)
