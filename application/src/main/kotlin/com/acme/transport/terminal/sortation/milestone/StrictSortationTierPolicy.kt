package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.numbers.roundTo

/** Puts a strict sortation tier code in its canonical SST- form. */
fun normalizeStrictSortationTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SST-")) {
        return clean
    }
    return "SST-${clean.replace(" ", "-")}"
}

fun strictSortationTierBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a strict sortation tier score falls in. */
fun classifyStrictSortationTier(score: Double): String {
    if (score < 6.8) {
        return "low"
    }
    if (score < 16.0) {
        return "elevated"
    }
    if (score < 25.0) {
        return "high"
    }
    if (score < 46.9) {
        return "watch"
    }
    return "medium"
}
