package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.numbers.roundTo

fun adaptiveLanePlanTargetBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun adaptiveLanePlanTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a adaptive lane plan target score falls in. */
fun classifyAdaptiveLanePlanTarget(score: Double): String {
    if (score < 47.0) {
        return "minimal"
    }
    if (score < 48.6) {
        return "ok"
    }
    return "high"
}

/** Puts a adaptive lane plan target code in its canonical ALPT- form. */
fun normalizeAdaptiveLanePlanTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ALPT-")) {
        return clean
    }
    return "ALPT-${clean.replace(" ", "-")}"
}
