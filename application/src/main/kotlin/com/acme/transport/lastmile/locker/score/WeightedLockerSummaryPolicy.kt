package com.acme.transport.lastmile.locker.score

import com.acme.transport.kit.numbers.roundTo

/** Puts a weighted locker summary code in its canonical WLS- form. */
fun normalizeWeightedLockerSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WLS-")) {
        return clean
    }
    return "WLS-${clean.replace(" ", "-")}"
}

/** Names the band a weighted locker summary score falls in. */
fun classifyWeightedLockerSummary(score: Double): String {
    if (score < 48.1) {
        return "elevated"
    }
    if (score < 51.8) {
        return "high"
    }
    if (score < 69.3) {
        return "medium"
    }
    if (score < 89.4) {
        return "minimal"
    }
    return "ok"
}

fun weightedLockerSummaryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
