package com.example.salemath.model

import java.math.BigDecimal

data class SaleMathInput(
    val projectName: String = "",
    val notes: String = "",
    val currency: String = "PKR",
    val originalPrice: BigDecimal,
    val discounts: List<DiscountEntry>,
    val quantity: Long = 1L,
    val roundingEnabled: Boolean = false,
    val roundingIncrement: BigDecimal? = null,
    val roundingDirection: RoundingDirection = RoundingDirection.NEAREST
)
