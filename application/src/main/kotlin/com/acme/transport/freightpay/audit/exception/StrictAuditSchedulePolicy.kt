package com.acme.transport.freightpay.audit.exception

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun strictAuditScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a strict audit schedule code in its canonical SAS- form. */
fun normalizeStrictAuditScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAS-")) {
        return clean
    }
    return "SAS-${clean.replace(" ", "-")}"
}

/** Names the band a strict audit schedule score falls in. */
fun classifyStrictAuditSchedule(score: Double): String {
    if (score < 23.5) {
        return "elevated"
    }
    if (score < 53.1) {
        return "low"
    }
    return "watch"
}
