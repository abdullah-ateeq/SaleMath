package com.example.salemath.calculator

import com.example.salemath.model.DiscountStep
import com.example.salemath.model.RoundingDirection
import com.example.salemath.model.SaleMathInput
import com.example.salemath.model.SaleMathResult
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object SaleMathCalculator {

    private val ONE_HUNDRED = BigDecimal("100")
    private val MATH_CONTEXT = MathContext.DECIMAL128

    /**
     * Calculates a single discount step sequentially.
     *
     * priceAfter = priceBefore * (1 - discountPercent / 100)
     * discountAmount = priceBefore - priceAfter
     */
    fun calculateDiscountStep(
        priceBefore: BigDecimal,
        discountPercent: BigDecimal,
        stepNumber: Int
    ): DiscountStep {
        val clampedBefore = priceBefore.max(BigDecimal.ZERO)
        val clampedPercent = discountPercent.coerceIn(BigDecimal.ZERO, ONE_HUNDRED)

        if (clampedBefore.compareTo(BigDecimal.ZERO) == 0 || clampedPercent.compareTo(BigDecimal.ZERO) == 0) {
            return DiscountStep(
                stepNumber = stepNumber,
                discountPercent = clampedPercent,
                priceBefore = clampedBefore,
                discountAmount = BigDecimal.ZERO,
                priceAfter = clampedBefore
            )
        }

        if (clampedPercent.compareTo(ONE_HUNDRED) == 0) {
            return DiscountStep(
                stepNumber = stepNumber,
                discountPercent = clampedPercent,
                priceBefore = clampedBefore,
                discountAmount = clampedBefore,
                priceAfter = BigDecimal.ZERO
            )
        }

        val discountFraction = clampedPercent.divide(ONE_HUNDRED, MATH_CONTEXT)
        val factor = BigDecimal.ONE.subtract(discountFraction, MATH_CONTEXT)
        val rawPriceAfter = clampedBefore.multiply(factor, MATH_CONTEXT)
        val priceAfter = rawPriceAfter.max(BigDecimal.ZERO)
        val discountAmount = clampedBefore.subtract(priceAfter, MATH_CONTEXT).max(BigDecimal.ZERO)

        return DiscountStep(
            stepNumber = stepNumber,
            discountPercent = clampedPercent,
            priceBefore = clampedBefore,
            discountAmount = discountAmount,
            priceAfter = priceAfter
        )
    }

    /**
     * Calculates final sale price applying each discount sequentially.
     */
    fun calculateFinalPrice(
        originalPrice: BigDecimal,
        discounts: List<BigDecimal>
    ): BigDecimal {
        var currentPrice = originalPrice.max(BigDecimal.ZERO)
        for (discount in discounts) {
            val step = calculateDiscountStep(currentPrice, discount, 0)
            currentPrice = step.priceAfter
        }
        return currentPrice
    }

    /**
     * Calculates total savings: originalPrice - finalPrice
     */
    fun calculateTotalSavings(
        originalPrice: BigDecimal,
        finalPrice: BigDecimal
    ): BigDecimal {
        val original = originalPrice.max(BigDecimal.ZERO)
        val finalP = finalPrice.max(BigDecimal.ZERO)
        return original.subtract(finalP, MATH_CONTEXT).max(BigDecimal.ZERO)
    }

    /**
     * Calculates effective discount percentage:
     * (originalPrice - finalPrice) / originalPrice * 100
     */
    fun calculateEffectiveDiscount(
        originalPrice: BigDecimal,
        finalPrice: BigDecimal
    ): BigDecimal {
        val original = originalPrice.max(BigDecimal.ZERO)
        val finalP = finalPrice.max(BigDecimal.ZERO)

        if (original.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO
        }
        if (finalP.compareTo(BigDecimal.ZERO) == 0) {
            return ONE_HUNDRED
        }

        val savings = calculateTotalSavings(original, finalP)
        return savings
            .divide(original, MATH_CONTEXT)
            .multiply(ONE_HUNDRED, MATH_CONTEXT)
            .coerceIn(BigDecimal.ZERO, ONE_HUNDRED)
    }

    /**
     * Calculates total original price for multiple quantities.
     */
    fun calculateTotalOriginal(
        originalPrice: BigDecimal,
        quantity: Long
    ): BigDecimal {
        val qty = quantity.coerceAtLeast(1L)
        return originalPrice.max(BigDecimal.ZERO).multiply(BigDecimal.valueOf(qty), MATH_CONTEXT)
    }

    /**
     * Calculates total final price for multiple quantities.
     */
    fun calculateTotalFinal(
        finalPrice: BigDecimal,
        quantity: Long
    ): BigDecimal {
        val qty = quantity.coerceAtLeast(1L)
        return finalPrice.max(BigDecimal.ZERO).multiply(BigDecimal.valueOf(qty), MATH_CONTEXT)
    }

    /**
     * Calculates total savings amount for multiple quantities.
     */
    fun calculateTotalSavingsAmount(
        savingsPerUnit: BigDecimal,
        quantity: Long
    ): BigDecimal {
        val qty = quantity.coerceAtLeast(1L)
        return savingsPerUnit.max(BigDecimal.ZERO).multiply(BigDecimal.valueOf(qty), MATH_CONTEXT)
    }

    /**
     * Rounds final price to a specified increment and direction.
     * When NEAREST and halfway, rounds upward (RoundingMode.HALF_UP).
     */
    fun roundFinalPrice(
        price: BigDecimal,
        increment: BigDecimal,
        direction: RoundingDirection
    ): BigDecimal {
        if (increment.compareTo(BigDecimal.ZERO) <= 0) {
            return price.max(BigDecimal.ZERO)
        }

        val ratio = price.divide(increment, MATH_CONTEXT)
        val roundingMode = when (direction) {
            RoundingDirection.NEAREST -> RoundingMode.HALF_UP
            RoundingDirection.UP -> RoundingMode.CEILING
            RoundingDirection.DOWN -> RoundingMode.FLOOR
        }

        val scaled = ratio.setScale(0, roundingMode)
        return scaled.multiply(increment, MATH_CONTEXT).max(BigDecimal.ZERO)
    }

    /**
     * Full calculation orchestrator for a given SaleMathInput.
     */
    fun calculateResult(input: SaleMathInput): SaleMathResult {
        val originalPrice = input.originalPrice.max(BigDecimal.ZERO)
        val steps = mutableListOf<DiscountStep>()

        var currentPrice = originalPrice
        input.discounts.forEachIndexed { index, discountEntry ->
            val step = calculateDiscountStep(
                priceBefore = currentPrice,
                discountPercent = discountEntry.percent,
                stepNumber = index + 1
            )
            steps.add(step)
            currentPrice = step.priceAfter
        }

        val calculatedFinalPrice = currentPrice
        val actualFinalPrice = if (input.roundingEnabled && input.roundingIncrement != null && input.roundingIncrement.compareTo(BigDecimal.ZERO) > 0) {
            roundFinalPrice(calculatedFinalPrice, input.roundingIncrement, input.roundingDirection)
        } else {
            calculatedFinalPrice
        }

        val totalSavings = calculateTotalSavings(originalPrice, actualFinalPrice)
        val effectiveDiscountPercent = calculateEffectiveDiscount(originalPrice, actualFinalPrice)

        val qty = input.quantity.coerceAtLeast(1L)
        val totalOriginal = calculateTotalOriginal(originalPrice, qty)
        val totalFinal = calculateTotalFinal(actualFinalPrice, qty)
        val totalSavingsAmount = calculateTotalSavingsAmount(totalSavings, qty)

        return SaleMathResult(
            originalPrice = originalPrice,
            calculatedFinalPrice = calculatedFinalPrice,
            actualFinalPrice = actualFinalPrice,
            totalSavings = totalSavings,
            effectiveDiscountPercent = effectiveDiscountPercent,
            steps = steps,
            quantity = qty,
            totalOriginal = totalOriginal,
            totalFinal = totalFinal,
            totalSavingsAmount = totalSavingsAmount
        )
    }
}
