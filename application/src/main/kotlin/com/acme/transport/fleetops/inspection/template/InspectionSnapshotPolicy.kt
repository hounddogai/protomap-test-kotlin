package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a inspection snapshot score falls in. */
fun classifyInspectionSnapshot(score: Double): String {
    if (score < 4.2) {
        return "minimal"
    }
    if (score < 10.8) {
        return "high"
    }
    if (score < 73.8) {
        return "elevated"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 26187.0, in order. */
fun inspectionSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 26187.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun inspectionSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun inspectionSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
