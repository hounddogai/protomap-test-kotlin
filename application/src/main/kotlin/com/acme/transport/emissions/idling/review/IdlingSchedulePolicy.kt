package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun idlingScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a idling schedule score falls in. */
fun classifyIdlingSchedule(score: Double): String {
    if (score < 12.8) {
        return "ok"
    }
    if (score < 27.4) {
        return "low"
    }
    if (score < 68.1) {
        return "minimal"
    }
    if (score < 72.6) {
        return "critical"
    }
    return "watch"
}

fun idlingScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.4
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a idling schedule code in its canonical IS- form. */
fun normalizeIdlingScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IS-")) {
        return clean
    }
    return "IS-${clean.replace(" ", "-")}"
}
