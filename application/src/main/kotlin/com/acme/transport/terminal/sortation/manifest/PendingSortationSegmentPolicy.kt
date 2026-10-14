package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 48553.0, in order. */
fun pendingSortationSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 48553.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a pending sortation segment score falls in. */
fun classifyPendingSortationSegment(score: Double): String {
    if (score < 9.8) {
        return "ok"
    }
    if (score < 23.0) {
        return "elevated"
    }
    if (score < 36.9) {
        return "low"
    }
    if (score < 93.0) {
        return "critical"
    }
    return "watch"
}

fun pendingSortationSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.61
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
