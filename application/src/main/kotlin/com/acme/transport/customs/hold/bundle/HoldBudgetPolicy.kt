package com.acme.transport.customs.hold.bundle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun holdBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun holdBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun holdBudgetBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a hold budget score falls in. */
fun classifyHoldBudget(score: Double): String {
    if (score < 35.2) {
        return "high"
    }
    if (score < 71.4) {
        return "low"
    }
    return "elevated"
}
