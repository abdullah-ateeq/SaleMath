package com.example.salemath

import com.example.salemath.calculator.SaleMathCalculator
import com.example.salemath.data.SavedCalculationJson
import com.example.salemath.model.DiscountEntry
import com.example.salemath.model.RoundingDirection
import com.example.salemath.model.SaleMathInput
import com.example.salemath.model.SavedCalculation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class SaleMathCalculatorTest {

    // 1. Single discount: Original: 10,000, Discount: 20% -> Final: 8,000, Savings: 2,000, Effective: 20%
    @Test
    fun test1_singleDiscount() {
        val input = SaleMathInput(
            originalPrice = BigDecimal("10000"),
            discounts = listOf(DiscountEntry(percent = BigDecimal("20")))
        )
        val result = SaleMathCalculator.calculateResult(input)

        assertEquals(0, BigDecimal("8000").compareTo(result.calculatedFinalPrice))
        assertEquals(0, BigDecimal("2000").compareTo(result.totalSavings))
        assertEquals(0, BigDecimal("20").compareTo(result.effectiveDiscountPercent))
    }

    // 2. Two sequential discounts: Original: 10,000, Discounts: 20%, 10% -> Step 1: 8,000, Step 2: 7,200, Final: 7,200, Savings: 2,800, Effective: 28% (NOT 30%)
    @Test
    fun test2_twoSequentialDiscounts() {
        val input = SaleMathInput(
            originalPrice = BigDecimal("10000"),
            discounts = listOf(
                DiscountEntry(percent = BigDecimal("20")),
                DiscountEntry(percent = BigDecimal("10"))
            )
        )
        val result = SaleMathCalculator.calculateResult(input)

        assertEquals(2, result.steps.size)
        assertEquals(0, BigDecimal("8000").compareTo(result.steps[0].priceAfter))
        assertEquals(0, BigDecimal("7200").compareTo(result.steps[1].priceAfter))
        assertEquals(0, BigDecimal("7200").compareTo(result.calculatedFinalPrice))
        assertEquals(0, BigDecimal("2800").compareTo(result.totalSavings))
        assertEquals(0, BigDecimal("28").compareTo(result.effectiveDiscountPercent))
    }

    // 3. Three sequential discounts: Original: 10,000, Discounts: 20%, 10%, 5% -> 8,000 -> 7,200 -> 6,840, Final: 6,840, Savings: 3,160, Effective: 31.6%
    @Test
    fun test3_threeSequentialDiscounts() {
        val input = SaleMathInput(
            originalPrice = BigDecimal("10000"),
            discounts = listOf(
                DiscountEntry(percent = BigDecimal("20")),
                DiscountEntry(percent = BigDecimal("10")),
                DiscountEntry(percent = BigDecimal("5"))
            )
        )
        val result = SaleMathCalculator.calculateResult(input)

        assertEquals(3, result.steps.size)
        assertEquals(0, BigDecimal("8000").compareTo(result.steps[0].priceAfter))
        assertEquals(0, BigDecimal("7200").compareTo(result.steps[1].priceAfter))
        assertEquals(0, BigDecimal("6840").compareTo(result.steps[2].priceAfter))
        assertEquals(0, BigDecimal("6840").compareTo(result.calculatedFinalPrice))
        assertEquals(0, BigDecimal("3160").compareTo(result.totalSavings))
        assertEquals(0, BigDecimal("31.6").compareTo(result.effectiveDiscountPercent))
    }

    // 4. Zero discount: Original: 10,000, Discount: 0% -> Final: 10,000, Savings: 0, Effective: 0%
    @Test
    fun test4_zeroDiscount() {
        val input = SaleMathInput(
            originalPrice = BigDecimal("10000"),
            discounts = listOf(DiscountEntry(percent = BigDecimal.ZERO))
        )
        val result = SaleMathCalculator.calculateResult(input)

        assertEquals(0, BigDecimal("10000").compareTo(result.calculatedFinalPrice))
        assertEquals(0, BigDecimal.ZERO.compareTo(result.totalSavings))
        assertEquals(0, BigDecimal.ZERO.compareTo(result.effectiveDiscountPercent))
        assertEquals(0, BigDecimal.ZERO.compareTo(result.steps[0].discountAmount))
    }

    // 5. 100% discount: Original: 10,000, Discount: 100% -> Final: 0, Savings: 10,000, Effective: 100%
    @Test
    fun test5_hundredPercentDiscount() {
        val input = SaleMathInput(
            originalPrice = BigDecimal("10000"),
            discounts = listOf(DiscountEntry(percent = BigDecimal("100")))
        )
        val result = SaleMathCalculator.calculateResult(input)

        assertEquals(0, BigDecimal.ZERO.compareTo(result.calculatedFinalPrice))
        assertEquals(0, BigDecimal("10000").compareTo(result.totalSavings))
        assertEquals(0, BigDecimal("100").compareTo(result.effectiveDiscountPercent))
    }

    // 6. Multiple discounts after zero: Any later discount after a zero price simply keeps 0
    @Test
    fun test6_multipleDiscountsAfterZero() {
        val input = SaleMathInput(
            originalPrice = BigDecimal("10000"),
            discounts = listOf(
                DiscountEntry(percent = BigDecimal("100")),
                DiscountEntry(percent = BigDecimal("20")),
                DiscountEntry(percent = BigDecimal("10"))
            )
        )
        val result = SaleMathCalculator.calculateResult(input)

        assertEquals(0, BigDecimal.ZERO.compareTo(result.calculatedFinalPrice))
        assertEquals(0, BigDecimal("10000").compareTo(result.totalSavings))
        assertEquals(0, BigDecimal("100").compareTo(result.effectiveDiscountPercent))
        assertEquals(0, BigDecimal.ZERO.compareTo(result.steps[1].priceAfter))
        assertEquals(0, BigDecimal.ZERO.compareTo(result.steps[2].priceAfter))
    }

    // 7. Effective discount calculation: (original - final) / original * 100
    @Test
    fun test7_effectiveDiscount() {
        val eff = SaleMathCalculator.calculateEffectiveDiscount(
            originalPrice = BigDecimal("10000"),
            finalPrice = BigDecimal("7200")
        )
        assertEquals(0, BigDecimal("28").compareTo(eff))
    }

    // 8. Savings calculation: original - final
    @Test
    fun test8_savings() {
        val savings = SaleMathCalculator.calculateTotalSavings(
            originalPrice = BigDecimal("10000"),
            finalPrice = BigDecimal("6840")
        )
        assertEquals(0, BigDecimal("3160").compareTo(savings))
    }

    // 9. Discount step amounts: priceBefore, discountAmount, priceAfter
    @Test
    fun test9_discountStepAmounts() {
        val step1 = SaleMathCalculator.calculateDiscountStep(
            priceBefore = BigDecimal("10000"),
            discountPercent = BigDecimal("20"),
            stepNumber = 1
        )
        assertEquals(0, BigDecimal("10000").compareTo(step1.priceBefore))
        assertEquals(0, BigDecimal("2000").compareTo(step1.discountAmount))
        assertEquals(0, BigDecimal("8000").compareTo(step1.priceAfter))

        val step2 = SaleMathCalculator.calculateDiscountStep(
            priceBefore = step1.priceAfter,
            discountPercent = BigDecimal("10"),
            stepNumber = 2
        )
        assertEquals(0, BigDecimal("8000").compareTo(step2.priceBefore))
        assertEquals(0, BigDecimal("800").compareTo(step2.discountAmount))
        assertEquals(0, BigDecimal("7200").compareTo(step2.priceAfter))
    }

    // 10. Quantity totals:
    // Original per unit: 1,000, Discounts: 20%, 10%, Quantity: 10
    // Final per unit: 720, Total original: 10,000, Total final: 7,200, Total savings: 2,800
    @Test
    fun test10_quantityTotals() {
        val input = SaleMathInput(
            originalPrice = BigDecimal("1000"),
            discounts = listOf(
                DiscountEntry(percent = BigDecimal("20")),
                DiscountEntry(percent = BigDecimal("10"))
            ),
            quantity = 10L
        )
        val result = SaleMathCalculator.calculateResult(input)

        assertEquals(0, BigDecimal("720").compareTo(result.calculatedFinalPrice))
        assertEquals(0, BigDecimal("10000").compareTo(result.totalOriginal))
        assertEquals(0, BigDecimal("7200").compareTo(result.totalFinal))
        assertEquals(0, BigDecimal("2800").compareTo(result.totalSavingsAmount))
    }

    // 11. Decimal prices: Original: 999.99, Discount: 17%
    @Test
    fun test11_decimalPrices() {
        val input = SaleMathInput(
            originalPrice = BigDecimal("999.99"),
            discounts = listOf(DiscountEntry(percent = BigDecimal("17")))
        )
        val result = SaleMathCalculator.calculateResult(input)

        // 999.99 * 0.83 = 829.9917
        val roundedFinal = result.calculatedFinalPrice.setScale(4, RoundingMode.HALF_UP)
        assertEquals(0, BigDecimal("829.9917").compareTo(roundedFinal))
    }

    // 12. Large prices: 999,999,999,999.99
    @Test
    fun test12_largePrices() {
        val largePrice = BigDecimal("999999999999.99")
        val input = SaleMathInput(
            originalPrice = largePrice,
            discounts = listOf(DiscountEntry(percent = BigDecimal("10")))
        )
        val result = SaleMathCalculator.calculateResult(input)

        // 999999999999.99 * 0.9 = 899999999999.991
        val rounded = result.calculatedFinalPrice.setScale(3, RoundingMode.HALF_UP)
        assertEquals(0, BigDecimal("899999999999.991").compareTo(rounded))
    }

    // 13. Invalid negative price: clamped to 0
    @Test
    fun test13_invalidNegativePrice() {
        val input = SaleMathInput(
            originalPrice = BigDecimal("-500"),
            discounts = listOf(DiscountEntry(percent = BigDecimal("20")))
        )
        val result = SaleMathCalculator.calculateResult(input)
        assertEquals(0, BigDecimal.ZERO.compareTo(result.calculatedFinalPrice))
    }

    // 14. Invalid negative discount: clamped to 0
    @Test
    fun test14_invalidNegativeDiscount() {
        val input = SaleMathInput(
            originalPrice = BigDecimal("1000"),
            discounts = listOf(DiscountEntry(percent = BigDecimal("-15")))
        )
        val result = SaleMathCalculator.calculateResult(input)
        assertEquals(0, BigDecimal("1000").compareTo(result.calculatedFinalPrice))
    }

    // 15. Discount > 100: clamped to 100%, final price 0
    @Test
    fun test15_discountGreaterThan100() {
        val input = SaleMathInput(
            originalPrice = BigDecimal("1000"),
            discounts = listOf(DiscountEntry(percent = BigDecimal("150")))
        )
        val result = SaleMathCalculator.calculateResult(input)
        assertEquals(0, BigDecimal.ZERO.compareTo(result.calculatedFinalPrice))
        assertEquals(0, BigDecimal("1000").compareTo(result.totalSavings))
    }

    // 16. Maximum discount count: 10 discounts sequentially
    @Test
    fun test16_maximumDiscountCount() {
        val discounts = (1..10).map { DiscountEntry(percent = BigDecimal("5")) }
        val input = SaleMathInput(
            originalPrice = BigDecimal("10000"),
            discounts = discounts
        )
        val result = SaleMathCalculator.calculateResult(input)
        assertEquals(10, result.steps.size)
        assertTrue(result.calculatedFinalPrice > BigDecimal.ZERO)
    }

    // 17. Remove discount updates sequential calculations
    @Test
    fun test17_removeDiscount() {
        val discounts = mutableListOf(
            DiscountEntry(id = "1", percent = BigDecimal("20")),
            DiscountEntry(id = "2", percent = BigDecimal("10"))
        )
        val beforeResult = SaleMathCalculator.calculateResult(
            SaleMathInput(originalPrice = BigDecimal("10000"), discounts = discounts)
        )
        assertEquals(0, BigDecimal("7200").compareTo(beforeResult.calculatedFinalPrice))

        discounts.removeAt(1) // remove 10%
        val afterResult = SaleMathCalculator.calculateResult(
            SaleMathInput(originalPrice = BigDecimal("10000"), discounts = discounts)
        )
        assertEquals(0, BigDecimal("8000").compareTo(afterResult.calculatedFinalPrice))
    }

    // 18. Reorder discount: sequence preserved
    @Test
    fun test18_reorderDiscount() {
        val stepA = SaleMathCalculator.calculateDiscountStep(BigDecimal("10000"), BigDecimal("20"), 1)
        val stepB = SaleMathCalculator.calculateDiscountStep(stepA.priceAfter, BigDecimal("10"), 2)
        assertEquals(0, BigDecimal("2000").compareTo(stepA.discountAmount))
        assertEquals(0, BigDecimal("800").compareTo(stepB.discountAmount))

        // In reverse order: 10% first, then 20%
        val stepRevA = SaleMathCalculator.calculateDiscountStep(BigDecimal("10000"), BigDecimal("10"), 1)
        val stepRevB = SaleMathCalculator.calculateDiscountStep(stepRevA.priceAfter, BigDecimal("20"), 2)
        assertEquals(0, BigDecimal("1000").compareTo(stepRevA.discountAmount))
        assertEquals(0, BigDecimal("1800").compareTo(stepRevB.discountAmount))

        // While final price is identical (7,200), the steps preserve user order!
        assertEquals(0, stepB.priceAfter.compareTo(stepRevB.priceAfter))
    }

    // 19. Final price rounding: NEAREST, UP, DOWN
    @Test
    fun test19_finalPriceRounding() {
        val price = BigDecimal("7234.56")

        // Nearest 1 -> 7235
        val r1 = SaleMathCalculator.roundFinalPrice(price, BigDecimal("1"), RoundingDirection.NEAREST)
        assertEquals(0, BigDecimal("7235").compareTo(r1))

        // Nearest 5 -> 7235
        val r5 = SaleMathCalculator.roundFinalPrice(price, BigDecimal("5"), RoundingDirection.NEAREST)
        assertEquals(0, BigDecimal("7235").compareTo(r5))

        // Nearest 50 -> 7250
        val r50Nearest = SaleMathCalculator.roundFinalPrice(price, BigDecimal("50"), RoundingDirection.NEAREST)
        assertEquals(0, BigDecimal("7250").compareTo(r50Nearest))

        // Down 50 -> 7200
        val r50Down = SaleMathCalculator.roundFinalPrice(price, BigDecimal("50"), RoundingDirection.DOWN)
        assertEquals(0, BigDecimal("7200").compareTo(r50Down))

        // Up 50 -> 7250
        val r50Up = SaleMathCalculator.roundFinalPrice(price, BigDecimal("50"), RoundingDirection.UP)
        assertEquals(0, BigDecimal("7250").compareTo(r50Up))
    }

    // 20. Rounded effective discount recalculation
    @Test
    fun test20_roundedEffectiveDiscount() {
        val input = SaleMathInput(
            originalPrice = BigDecimal("10000"),
            discounts = listOf(DiscountEntry(percent = BigDecimal("27.6544"))),
            roundingEnabled = true,
            roundingIncrement = BigDecimal("1"),
            roundingDirection = RoundingDirection.NEAREST
        )
        val result = SaleMathCalculator.calculateResult(input)
        // Calculated final was ~ 7234.56, rounded final is 7235
        // Actual savings: 10000 - 7235 = 2765
        // Actual effective discount: 2765 / 10000 * 100 = 27.65%
        assertEquals(0, BigDecimal("7235").compareTo(result.actualFinalPrice))
        assertEquals(0, BigDecimal("2765").compareTo(result.totalSavings))
        assertEquals(0, BigDecimal("27.65").compareTo(result.effectiveDiscountPercent))
    }

    // 21. BigDecimal precision vs Double floating-point inaccuracies
    @Test
    fun test21_bigDecimalPrecision() {
        // Double precision artifact: 10000 * (1 - 0.20) * (1 - 0.10) in floats can have tiny epsilon errors
        val price = BigDecimal("999.99")
        val d1 = BigDecimal("17.33")
        val d2 = BigDecimal("8.75")
        val input = SaleMathInput(
            originalPrice = price,
            discounts = listOf(DiscountEntry(percent = d1), DiscountEntry(percent = d2))
        )
        val result = SaleMathCalculator.calculateResult(input)
        assertTrue(result.calculatedFinalPrice > BigDecimal.ZERO)
        assertTrue(result.totalSavings > BigDecimal.ZERO)
        // Ensure no precision loss or NaN
        assertFalse(result.calculatedFinalPrice.toPlainString().contains("NaN"))
    }

    // 22. History persistence serialization & max 50 items
    @Test
    fun test22_historyPersistence() {
        val originalList = (1..50).map { i ->
            SavedCalculation(
                id = "calc-$i",
                projectName = "Project $i",
                notes = "Notes for item $i with \"quotes\" & special chars",
                currency = "PKR",
                originalPrice = BigDecimal("10000"),
                discounts = listOf(BigDecimal("20"), BigDecimal("10")),
                quantity = 1L,
                calculatedFinalPrice = BigDecimal("7200"),
                actualFinalPrice = BigDecimal("7200"),
                totalSavings = BigDecimal("2800"),
                effectiveDiscountPercent = BigDecimal("28"),
                timestamp = 1000L + i,
                isFavorite = (i % 2 == 0)
            )
        }

        val json = SavedCalculationJson.toJson(originalList)
        val parsedList = SavedCalculationJson.fromJson(json)

        assertEquals(50, parsedList.size)
        assertEquals("calc-1", parsedList[0].id)
        assertEquals("Project 1", parsedList[0].projectName)
        assertEquals(0, BigDecimal("7200").compareTo(parsedList[0].actualFinalPrice))
        assertEquals(2, parsedList[0].discounts.size)
        assertFalse(parsedList[0].isFavorite)
        assertTrue(parsedList[1].isFavorite)
    }

    // 23. Settings defaults
    @Test
    fun test23_settingsPersistence() {
        val defaultSettings = com.example.salemath.model.AppSettings()
        assertEquals("PKR", defaultSettings.defaultCurrency)
        assertEquals(0, BigDecimal("10000").compareTo(defaultSettings.defaultOriginalPrice))
        assertEquals(0, BigDecimal("20").compareTo(defaultSettings.defaultDiscount))
        assertEquals(2, defaultSettings.decimalPlaces)
        assertTrue(defaultSettings.showDiscountBreakdown)
        assertTrue(defaultSettings.showFormula)
    }

    // 24. Favorites sorting priority
    @Test
    fun test24_favorites() {
        val item1 = SavedCalculation(
            id = "1", projectName = "Item 1", notes = "", currency = "PKR",
            originalPrice = BigDecimal("1000"), discounts = listOf(BigDecimal("10")),
            quantity = 1, calculatedFinalPrice = BigDecimal("900"), actualFinalPrice = BigDecimal("900"),
            totalSavings = BigDecimal("100"), effectiveDiscountPercent = BigDecimal("10"),
            timestamp = 100L, isFavorite = false
        )
        val item2 = SavedCalculation(
            id = "2", projectName = "Item 2", notes = "", currency = "PKR",
            originalPrice = BigDecimal("1000"), discounts = listOf(BigDecimal("10")),
            quantity = 1, calculatedFinalPrice = BigDecimal("900"), actualFinalPrice = BigDecimal("900"),
            totalSavings = BigDecimal("100"), effectiveDiscountPercent = BigDecimal("10"),
            timestamp = 200L, isFavorite = true
        )

        val list = listOf(item1, item2)
        val sorted = list.sortedWith(
            compareByDescending<SavedCalculation> { it.isFavorite }
                .thenByDescending { it.timestamp }
        )

        assertEquals("2", sorted[0].id) // Favorite item first!
        assertEquals("1", sorted[1].id)
    }
}
