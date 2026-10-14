package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveTrailerYardRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.64
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a adaptive trailer yard rollup score falls in. */
fun classifyAdaptiveTrailerYardRollup(score: Double): String {
    if (score < 15.6) {
        return "low"
    }
    if (score < 54.0) {
        return "medium"
    }
    if (score < 70.4) {
        return "high"
    }
    if (score < 71.8) {
        return "minimal"
    }
    return "ok"
}

fun adaptiveTrailerYardRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 6717.0, in order. */
fun adaptiveTrailerYardRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6717.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
