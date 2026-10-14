package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun dutyPeriodRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a duty period rollup score falls in. */
fun classifyDutyPeriodRollup(score: Double): String {
    if (score < 1.3) {
        return "minimal"
    }
    if (score < 5.0) {
        return "elevated"
    }
    if (score < 22.2) {
        return "medium"
    }
    if (score < 22.6) {
        return "low"
    }
    return "high"
}

fun dutyPeriodRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.25
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a duty period rollup code in its canonical DPR- form. */
fun normalizeDutyPeriodRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPR-")) {
        return clean
    }
    return "DPR-${clean.replace(" ", "-")}"
}
