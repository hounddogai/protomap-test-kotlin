package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun dutyPeriodBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a duty period budget score falls in. */
fun classifyDutyPeriodBudget(score: Double): String {
    if (score < 33.0) {
        return "low"
    }
    if (score < 99.2) {
        return "high"
    }
    return "watch"
}

/** Puts a duty period budget code in its canonical DPB- form. */
fun normalizeDutyPeriodBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPB-")) {
        return clean
    }
    return "DPB-${clean.replace(" ", "-")}"
}

fun dutyPeriodBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.13
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
