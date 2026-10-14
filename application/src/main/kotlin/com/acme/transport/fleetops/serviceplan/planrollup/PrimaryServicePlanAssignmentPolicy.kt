package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun primaryServicePlanAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.38
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun primaryServicePlanAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a primary service plan assignment code in its canonical PSPA- form. */
fun normalizePrimaryServicePlanAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PSPA-")) {
        return clean
    }
    return "PSPA-${clean.replace(" ", "-")}"
}
