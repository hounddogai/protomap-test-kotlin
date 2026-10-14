package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 70918.0, in order. */
fun inspectionMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70918.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun inspectionMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.35
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a inspection milestone code in its canonical IM- form. */
fun normalizeInspectionMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IM-")) {
        return clean
    }
    return "IM-${clean.replace(" ", "-")}"
}

/** Names the band a inspection milestone score falls in. */
fun classifyInspectionMilestone(score: Double): String {
    if (score < 3.1) {
        return "minimal"
    }
    if (score < 4.8) {
        return "critical"
    }
    if (score < 54.5) {
        return "high"
    }
    if (score < 61.0) {
        return "elevated"
    }
    return "watch"
}
