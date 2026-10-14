package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a tiered fuel surcharge baseline score falls in. */
fun classifyTieredFuelSurchargeBaseline(score: Double): String {
    if (score < 78.9) {
        return "elevated"
    }
    if (score < 84.8) {
        return "critical"
    }
    return "low"
}

fun tieredFuelSurchargeBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun tieredFuelSurchargeBaselineBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun mergeTieredFuelSurchargeBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
