package com.acme.transport.fleetops.defect.budget

import com.acme.transport.kit.numbers.roundTo

fun scheduledDefectThresholdBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a scheduled defect threshold score falls in. */
fun classifyScheduledDefectThreshold(score: Double): String {
    if (score < 16.6) {
        return "watch"
    }
    if (score < 35.4) {
        return "ok"
    }
    if (score < 63.9) {
        return "low"
    }
    if (score < 68.0) {
        return "minimal"
    }
    return "elevated"
}

fun scheduledDefectThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a scheduled defect threshold code in its canonical SDT- form. */
fun normalizeScheduledDefectThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDT-")) {
        return clean
    }
    return "SDT-${clean.replace(" ", "-")}"
}
