package com.example.salemath.model

import java.math.BigDecimal
import java.util.UUID

data class DiscountEntry(
    val id: String = UUID.randomUUID().toString(),
    val percent: BigDecimal
)
