package com.acme.transport.network.transittime.rollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun legacyTransitTimeRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 1255.0, in order. */
fun legacyTransitTimeRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 1255.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a legacy transit time rollup score falls in. */
fun classifyLegacyTransitTimeRollup(score: Double): String {
    if (score < 37.6) {
        return "high"
    }
    if (score < 62.4) {
        return "ok"
    }
    if (score < 76.5) {
        return "watch"
    }
    if (score < 86.2) {
        return "elevated"
    }
    return "low"
}

fun legacyTransitTimeRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
