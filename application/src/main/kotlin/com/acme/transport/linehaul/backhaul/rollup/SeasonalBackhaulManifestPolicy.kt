package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 6148.0, in order. */
fun seasonalBackhaulManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6148.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a seasonal backhaul manifest score falls in. */
fun classifySeasonalBackhaulManifest(score: Double): String {
    if (score < 29.2) {
        return "low"
    }
    if (score < 50.4) {
        return "minimal"
    }
    if (score < 64.9) {
        return "medium"
    }
    return "watch"
}

fun seasonalBackhaulManifestBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a seasonal backhaul manifest code in its canonical SBM- form. */
fun normalizeSeasonalBackhaulManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SBM-")) {
        return clean
    }
    return "SBM-${clean.replace(" ", "-")}"
}
