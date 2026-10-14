package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.numbers.roundTo

/** Names the band a tiered cross dock profile score falls in. */
fun classifyTieredCrossDockProfile(score: Double): String {
    if (score < 68.5) {
        return "medium"
    }
    if (score < 78.3) {
        return "ok"
    }
    return "minimal"
}

fun tieredCrossDockProfileBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun tieredCrossDockProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Puts a tiered cross dock profile code in its canonical TCDP- form. */
fun normalizeTieredCrossDockProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TCDP-")) {
        return clean
    }
    return "TCDP-${clean.replace(" ", "-")}"
}
