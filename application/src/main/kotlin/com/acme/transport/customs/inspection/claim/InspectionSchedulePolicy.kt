package com.acme.transport.customs.inspection.claim

import com.acme.transport.kit.numbers.roundTo

fun inspectionScheduleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a inspection schedule code in its canonical IS- form. */
fun normalizeInspectionScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IS-")) {
        return clean
    }
    return "IS-${clean.replace(" ", "-")}"
}

/** Names the band a inspection schedule score falls in. */
fun classifyInspectionSchedule(score: Double): String {
    if (score < 5.2) {
        return "low"
    }
    if (score < 62.7) {
        return "minimal"
    }
    return "critical"
}
