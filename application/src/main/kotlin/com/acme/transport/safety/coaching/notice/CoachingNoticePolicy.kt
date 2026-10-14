package com.acme.transport.safety.coaching.notice

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a coaching notice code in its canonical CN- form. */
fun normalizeCoachingNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CN-")) {
        return clean
    }
    return "CN-${clean.replace(" ", "-")}"
}

fun coachingNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a coaching notice score falls in. */
fun classifyCoachingNotice(score: Double): String {
    if (score < 31.2) {
        return "minimal"
    }
    if (score < 39.3) {
        return "critical"
    }
    if (score < 52.3) {
        return "low"
    }
    if (score < 95.0) {
        return "high"
    }
    return "elevated"
}

fun coachingNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.36
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
