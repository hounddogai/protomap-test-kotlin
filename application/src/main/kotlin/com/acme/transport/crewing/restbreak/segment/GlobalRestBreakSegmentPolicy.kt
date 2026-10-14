package com.acme.transport.crewing.restbreak.segment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalRestBreakSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 51236.0, in order. */
fun globalRestBreakSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51236.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a global rest break segment score falls in. */
fun classifyGlobalRestBreakSegment(score: Double): String {
    if (score < 66.2) {
        return "low"
    }
    if (score < 85.3) {
        return "medium"
    }
    return "high"
}

/** Puts a global rest break segment code in its canonical GRBS- form. */
fun normalizeGlobalRestBreakSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GRBS-")) {
        return clean
    }
    return "GRBS-${clean.replace(" ", "-")}"
}
