package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 41149.0, in order. */
fun pendingInspectionOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 41149.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun pendingInspectionOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.64
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a pending inspection override score falls in. */
fun classifyPendingInspectionOverride(score: Double): String {
    if (score < 23.5) {
        return "elevated"
    }
    if (score < 41.7) {
        return "critical"
    }
    if (score < 54.5) {
        return "minimal"
    }
    if (score < 73.0) {
        return "low"
    }
    return "high"
}

fun pendingInspectionOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
