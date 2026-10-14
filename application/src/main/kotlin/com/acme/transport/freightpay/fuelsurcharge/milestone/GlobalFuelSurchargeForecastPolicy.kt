package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun globalFuelSurchargeForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun mergeGlobalFuelSurchargeForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a global fuel surcharge forecast score falls in. */
fun classifyGlobalFuelSurchargeForecast(score: Double): String {
    if (score < 65.4) {
        return "elevated"
    }
    if (score < 67.8) {
        return "critical"
    }
    if (score < 90.2) {
        return "high"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 50952.0, in order. */
fun globalFuelSurchargeForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 50952.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
