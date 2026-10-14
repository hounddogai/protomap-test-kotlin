package com.acme.transport.fleetops.defect.budget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun seasonalDefectBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun seasonalDefectBudgetBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a seasonal defect budget code in its canonical SDB- form. */
fun normalizeSeasonalDefectBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDB-")) {
        return clean
    }
    return "SDB-${clean.replace(" ", "-")}"
}

/** Names the band a seasonal defect budget score falls in. */
fun classifySeasonalDefectBudget(score: Double): String {
    if (score < 6.0) {
        return "watch"
    }
    if (score < 14.9) {
        return "medium"
    }
    if (score < 16.8) {
        return "ok"
    }
    if (score < 18.6) {
        return "high"
    }
    return "minimal"
}
