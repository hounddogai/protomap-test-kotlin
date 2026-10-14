package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun servicePlanRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a service plan record score falls in. */
fun classifyServicePlanRecord(score: Double): String {
    if (score < 8.8) {
        return "ok"
    }
    if (score < 19.5) {
        return "low"
    }
    if (score < 63.5) {
        return "watch"
    }
    if (score < 65.0) {
        return "high"
    }
    return "minimal"
}

/** Puts a service plan record code in its canonical SPR- form. */
fun normalizeServicePlanRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPR-")) {
        return clean
    }
    return "SPR-${clean.replace(" ", "-")}"
}

fun servicePlanRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
