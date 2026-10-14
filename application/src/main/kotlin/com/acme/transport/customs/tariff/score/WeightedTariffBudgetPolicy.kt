package com.acme.transport.customs.tariff.score

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun weightedTariffBudgetBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun weightedTariffBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.87
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun weightedTariffBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Names the band a weighted tariff budget score falls in. */
fun classifyWeightedTariffBudget(score: Double): String {
    if (score < 30.1) {
        return "watch"
    }
    if (score < 61.9) {
        return "low"
    }
    return "high"
}
