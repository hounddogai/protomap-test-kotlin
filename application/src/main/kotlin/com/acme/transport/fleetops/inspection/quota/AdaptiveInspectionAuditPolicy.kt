package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun adaptiveInspectionAuditBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun adaptiveInspectionAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 54887.0, in order. */
fun adaptiveInspectionAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54887.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a adaptive inspection audit score falls in. */
fun classifyAdaptiveInspectionAudit(score: Double): String {
    if (score < 43.3) {
        return "medium"
    }
    if (score < 49.5) {
        return "critical"
    }
    if (score < 65.7) {
        return "high"
    }
    if (score < 82.6) {
        return "ok"
    }
    return "minimal"
}
