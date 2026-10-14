package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a primary service plan notice score falls in. */
fun classifyPrimaryServicePlanNotice(score: Double): String {
    if (score < 8.0) {
        return "elevated"
    }
    if (score < 17.8) {
        return "watch"
    }
    if (score < 84.8) {
        return "high"
    }
    return "critical"
}

fun primaryServicePlanNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.58
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun primaryServicePlanNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a primary service plan notice code in its canonical PSPN- form. */
fun normalizePrimaryServicePlanNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PSPN-")) {
        return clean
    }
    return "PSPN-${clean.replace(" ", "-")}"
}
