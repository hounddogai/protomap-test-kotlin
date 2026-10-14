package com.acme.transport.terminal.traileryard.score

import com.acme.transport.kit.numbers.roundTo

/** Puts a weighted trailer yard allocation code in its canonical WTYA- form. */
fun normalizeWeightedTrailerYardAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WTYA-")) {
        return clean
    }
    return "WTYA-${clean.replace(" ", "-")}"
}

/** Names the band a weighted trailer yard allocation score falls in. */
fun classifyWeightedTrailerYardAllocation(score: Double): String {
    if (score < 3.9) {
        return "low"
    }
    if (score < 16.6) {
        return "ok"
    }
    if (score < 25.0) {
        return "elevated"
    }
    return "critical"
}

fun weightedTrailerYardAllocationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
