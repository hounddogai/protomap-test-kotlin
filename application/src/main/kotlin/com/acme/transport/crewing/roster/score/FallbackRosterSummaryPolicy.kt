package com.acme.transport.crewing.roster.score

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun fallbackRosterSummaryBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 94414.0, in order. */
fun fallbackRosterSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94414.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a fallback roster summary score falls in. */
fun classifyFallbackRosterSummary(score: Double): String {
    if (score < 37.9) {
        return "elevated"
    }
    if (score < 54.3) {
        return "ok"
    }
    if (score < 69.4) {
        return "critical"
    }
    return "watch"
}

fun fallbackRosterSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.58
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
