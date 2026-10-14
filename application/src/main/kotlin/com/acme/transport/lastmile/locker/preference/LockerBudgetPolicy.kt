package com.acme.transport.lastmile.locker.preference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a locker budget score falls in. */
fun classifyLockerBudget(score: Double): String {
    if (score < 33.2) {
        return "elevated"
    }
    if (score < 37.9) {
        return "minimal"
    }
    if (score < 84.4) {
        return "low"
    }
    if (score < 93.7) {
        return "watch"
    }
    return "ok"
}

fun lockerBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun lockerBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.83
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
