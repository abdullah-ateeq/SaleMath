package com.example.salemath.data

import com.example.salemath.model.RoundingDirection
import com.example.salemath.model.SavedCalculation
import java.math.BigDecimal

object SavedCalculationJson {

    private fun escape(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun unescape(s: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                when (val next = s[i + 1]) {
                    '\\' -> { sb.append('\\'); i += 2 }
                    '"' -> { sb.append('"'); i += 2 }
                    'b' -> { sb.append('\b'); i += 2 }
                    'n' -> { sb.append('\n'); i += 2 }
                    'r' -> { sb.append('\r'); i += 2 }
                    't' -> { sb.append('\t'); i += 2 }
                    else -> { sb.append(next); i += 2 }
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    fun toJson(list: List<SavedCalculation>): String {
        return buildString {
            append("[\n")
            list.forEachIndexed { i, item ->
                append("  {\n")
                append("    \"id\": \"${escape(item.id)}\",\n")
                append("    \"projectName\": \"${escape(item.projectName)}\",\n")
                append("    \"notes\": \"${escape(item.notes)}\",\n")
                append("    \"currency\": \"${escape(item.currency)}\",\n")
                append("    \"originalPrice\": \"${item.originalPrice.toPlainString()}\",\n")
                append("    \"discounts\": [${item.discounts.joinToString(", ") { "\"${it.toPlainString()}\"" }}],\n")
                append("    \"quantity\": ${item.quantity},\n")
                append("    \"calculatedFinalPrice\": \"${item.calculatedFinalPrice.toPlainString()}\",\n")
                append("    \"actualFinalPrice\": \"${item.actualFinalPrice.toPlainString()}\",\n")
                append("    \"totalSavings\": \"${item.totalSavings.toPlainString()}\",\n")
                append("    \"effectiveDiscountPercent\": \"${item.effectiveDiscountPercent.toPlainString()}\",\n")
                append("    \"timestamp\": ${item.timestamp},\n")
                append("    \"isFavorite\": ${item.isFavorite},\n")
                append("    \"roundingEnabled\": ${item.roundingEnabled},\n")
                append("    \"roundingIncrement\": ${if (item.roundingIncrement != null) "\"${item.roundingIncrement.toPlainString()}\"" else "null"},\n")
                append("    \"roundingDirection\": \"${item.roundingDirection.name}\"\n")
                append("  }${if (i < list.size - 1) "," else ""}\n")
            }
            append("]")
        }
    }

    fun fromJson(jsonStr: String): List<SavedCalculation> {
        val trimmed = jsonStr.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return emptyList()
        }

        val results = mutableListOf<SavedCalculation>()
        var depth = 0
        var inQuotes = false
        var isEscape = false
        var objStart = -1

        for (i in trimmed.indices) {
            val c = trimmed[i]
            if (isEscape) {
                isEscape = false
                continue
            }
            if (c == '\\') {
                isEscape = true
                continue
            }
            if (c == '"') {
                inQuotes = !inQuotes
                continue
            }
            if (inQuotes) continue

            if (c == '{') {
                if (depth == 0) {
                    objStart = i
                }
                depth++
            } else if (c == '}') {
                depth--
                if (depth == 0 && objStart != -1) {
                    val objStr = trimmed.substring(objStart + 1, i)
                    parseObject(objStr)?.let { results.add(it) }
                    objStart = -1
                }
            }
        }

        return results
    }

    private fun parseObject(objContent: String): SavedCalculation? {
        val map = mutableMapOf<String, String>()
        var key = ""
        var inKey = false
        var inVal = false
        var inValQuotes = false
        var inArray = false
        var isEscape = false
        val keyBuf = StringBuilder()
        val valBuf = StringBuilder()

        var i = 0
        while (i < objContent.length) {
            val c = objContent[i]
            if (isEscape) {
                isEscape = false
                if (inKey) keyBuf.append(c)
                if (inVal) valBuf.append(c)
                i++
                continue
            }
            if (c == '\\') {
                isEscape = true
                if (inKey) keyBuf.append(c)
                if (inVal) valBuf.append(c)
                i++
                continue
            }

            if (!inVal) {
                if (c == '"') {
                    if (inKey) {
                        key = keyBuf.toString().trim()
                        keyBuf.clear()
                        inKey = false
                    } else {
                        inKey = true
                    }
                } else if (inKey) {
                    keyBuf.append(c)
                } else if (c == ':') {
                    inVal = true
                    valBuf.clear()
                    inValQuotes = false
                    inArray = false
                }
            } else {
                // We are reading a value
                if (c == '[' && !inValQuotes) {
                    inArray = true
                    valBuf.append(c)
                } else if (c == ']' && inArray && !inValQuotes) {
                    inArray = false
                    valBuf.append(c)
                } else if (c == '"' && !inArray) {
                    inValQuotes = !inValQuotes
                    valBuf.append(c)
                } else if (c == ',' && !inValQuotes && !inArray) {
                    // end of key-value
                    map[key] = valBuf.toString().trim()
                    inVal = false
                    key = ""
                } else {
                    valBuf.append(c)
                }
            }
            i++
        }

        if (key.isNotEmpty() && inVal) {
            map[key] = valBuf.toString().trim()
        }

        val id = cleanString(map["id"] ?: return null)
        val projectName = cleanString(map["projectName"] ?: "")
        val notes = cleanString(map["notes"] ?: "")
        val currency = cleanString(map["currency"] ?: "PKR")
        val originalPrice = parseBigDecimal(map["originalPrice"]) ?: BigDecimal.ZERO
        val discounts = parseBigDecimalList(map["discounts"] ?: "[]")
        val quantity = map["quantity"]?.toLongOrNull() ?: 1L
        val calculatedFinalPrice = parseBigDecimal(map["calculatedFinalPrice"]) ?: BigDecimal.ZERO
        val actualFinalPrice = parseBigDecimal(map["actualFinalPrice"]) ?: BigDecimal.ZERO
        val totalSavings = parseBigDecimal(map["totalSavings"]) ?: BigDecimal.ZERO
        val effectiveDiscountPercent = parseBigDecimal(map["effectiveDiscountPercent"]) ?: BigDecimal.ZERO
        val timestamp = map["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
        val isFavorite = map["isFavorite"]?.toBoolean() ?: false
        val roundingEnabled = map["roundingEnabled"]?.toBoolean() ?: false
        val roundingIncrement = parseBigDecimal(map["roundingIncrement"])
        val roundingDirectionStr = cleanString(map["roundingDirection"] ?: "NEAREST")
        val roundingDirection = try {
            RoundingDirection.valueOf(roundingDirectionStr)
        } catch (_: Exception) {
            RoundingDirection.NEAREST
        }

        return SavedCalculation(
            id = id,
            projectName = projectName,
            notes = notes,
            currency = currency,
            originalPrice = originalPrice,
            discounts = discounts,
            quantity = quantity,
            calculatedFinalPrice = calculatedFinalPrice,
            actualFinalPrice = actualFinalPrice,
            totalSavings = totalSavings,
            effectiveDiscountPercent = effectiveDiscountPercent,
            timestamp = timestamp,
            isFavorite = isFavorite,
            roundingEnabled = roundingEnabled,
            roundingIncrement = roundingIncrement,
            roundingDirection = roundingDirection
        )
    }

    private fun cleanString(raw: String): String {
        var s = raw.trim()
        if (s.startsWith("\"") && s.endsWith("\"") && s.length >= 2) {
            s = s.substring(1, s.length - 1)
        }
        return unescape(s)
    }

    private fun parseBigDecimal(raw: String?): BigDecimal? {
        if (raw == null || raw == "null") return null
        val cleaned = cleanString(raw)
        return try {
            BigDecimal(cleaned)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseBigDecimalList(raw: String): List<BigDecimal> {
        val trimmed = raw.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return emptyList()
        val inner = trimmed.substring(1, trimmed.length - 1).trim()
        if (inner.isEmpty()) return emptyList()
        return inner.split(",").mapNotNull {
            parseBigDecimal(it.trim())
        }
    }
}
