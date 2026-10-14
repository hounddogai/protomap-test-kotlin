package com.acme.transport.crewing.dutyperiod.perioddraft

/** Names the band a duty period adjustment score falls in. */
fun classifyDutyPeriodAdjustment(score: Double): String {
    if (score < 29.0) {
        return "low"
    }
    if (score < 81.7) {
        return "ok"
    }
    if (score < 94.2) {
        return "watch"
    }
    if (score < 97.3) {
        return "elevated"
    }
    return "minimal"
}

/** Puts a duty period adjustment code in its canonical DPA- form. */
fun normalizeDutyPeriodAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPA-")) {
        return clean
    }
    return "DPA-${clean.replace(" ", "-")}"
}

fun dutyPeriodAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
