package com.acme.transport.crewing.restbreak.preference

import com.acme.transport.kit.numbers.roundTo

fun restBreakPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a rest break preference score falls in. */
fun classifyRestBreakPreference(score: Double): String {
    if (score < 1.3) {
        return "critical"
    }
    if (score < 23.9) {
        return "low"
    }
    if (score < 71.1) {
        return "minimal"
    }
    return "medium"
}

fun restBreakPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a rest break preference code in its canonical RBP- form. */
fun normalizeRestBreakPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBP-")) {
        return clean
    }
    return "RBP-${clean.replace(" ", "-")}"
}
