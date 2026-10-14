package com.acme.transport.customs.clearance.profile

import com.acme.transport.kit.numbers.roundTo

fun primaryClearanceProfileBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a primary clearance profile code in its canonical PCP- form. */
fun normalizePrimaryClearanceProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCP-")) {
        return clean
    }
    return "PCP-${clean.replace(" ", "-")}"
}

fun primaryClearanceProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a primary clearance profile score falls in. */
fun classifyPrimaryClearanceProfile(score: Double): String {
    if (score < 86.1) {
        return "medium"
    }
    if (score < 87.3) {
        return "elevated"
    }
    return "high"
}
