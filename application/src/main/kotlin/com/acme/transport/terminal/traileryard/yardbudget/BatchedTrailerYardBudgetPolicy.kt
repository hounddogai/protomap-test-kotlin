package com.acme.transport.terminal.traileryard.yardbudget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a batched trailer yard budget score falls in. */
fun classifyBatchedTrailerYardBudget(score: Double): String {
    if (score < 14.1) {
        return "watch"
    }
    if (score < 24.2) {
        return "low"
    }
    if (score < 50.8) {
        return "minimal"
    }
    if (score < 75.1) {
        return "ok"
    }
    return "elevated"
}

fun batchedTrailerYardBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun batchedTrailerYardBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.22
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
