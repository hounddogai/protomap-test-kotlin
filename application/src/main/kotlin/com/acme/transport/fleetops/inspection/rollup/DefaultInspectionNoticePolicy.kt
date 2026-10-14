package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.kit.numbers.roundTo

fun defaultInspectionNoticeBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a default inspection notice score falls in. */
fun classifyDefaultInspectionNotice(score: Double): String {
    if (score < 48.6) {
        return "ok"
    }
    if (score < 71.6) {
        return "high"
    }
    if (score < 83.9) {
        return "watch"
    }
    if (score < 84.3) {
        return "medium"
    }
    return "critical"
}

fun defaultInspectionNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Puts a default inspection notice code in its canonical DIN- form. */
fun normalizeDefaultInspectionNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DIN-")) {
        return clean
    }
    return "DIN-${clean.replace(" ", "-")}"
}
