package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a inspection notice score falls in. */
fun classifyInspectionNotice(score: Double): String {
    if (score < 20.6) {
        return "ok"
    }
    if (score < 45.9) {
        return "critical"
    }
    return "high"
}

/** Puts a inspection notice code in its canonical IN- form. */
fun normalizeInspectionNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IN-")) {
        return clean
    }
    return "IN-${clean.replace(" ", "-")}"
}

fun inspectionNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeInspectionNotice(): Int {
    val policy = defaultPolicy("inspection-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
