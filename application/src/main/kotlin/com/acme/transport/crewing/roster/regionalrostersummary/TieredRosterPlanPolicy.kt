package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tieredRosterPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.21
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a tiered roster plan code in its canonical TRP- form. */
fun normalizeTieredRosterPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TRP-")) {
        return clean
    }
    return "TRP-${clean.replace(" ", "-")}"
}

/** Names the band a tiered roster plan score falls in. */
fun classifyTieredRosterPlan(score: Double): String {
    if (score < 8.0) {
        return "elevated"
    }
    if (score < 65.5) {
        return "watch"
    }
    return "ok"
}

fun tieredRosterPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
