package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 62893.0, in order. */
fun scheduledDwellAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62893.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a scheduled dwell adjustment score falls in. */
fun classifyScheduledDwellAdjustment(score: Double): String {
    if (score < 27.6) {
        return "critical"
    }
    if (score < 92.8) {
        return "high"
    }
    return "watch"
}

fun scheduledDwellAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun scheduledDwellAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.1
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
