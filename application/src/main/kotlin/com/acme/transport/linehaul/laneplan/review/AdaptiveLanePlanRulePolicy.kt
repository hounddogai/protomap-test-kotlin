package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a adaptive lane plan rule code in its canonical ALPR- form. */
fun normalizeAdaptiveLanePlanRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ALPR-")) {
        return clean
    }
    return "ALPR-${clean.replace(" ", "-")}"
}

fun adaptiveLanePlanRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun adaptiveLanePlanRuleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun adaptiveLanePlanRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
