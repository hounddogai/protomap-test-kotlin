package com.acme.transport.network.servicearea.snapshot

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 75257.0, in order. */
fun globalServiceAreaSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75257.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalServiceAreaSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a global service area snapshot score falls in. */
fun classifyGlobalServiceAreaSnapshot(score: Double): String {
    if (score < 11.4) {
        return "critical"
    }
    if (score < 29.7) {
        return "high"
    }
    if (score < 73.6) {
        return "elevated"
    }
    if (score < 78.8) {
        return "watch"
    }
    return "minimal"
}

/** Puts a global service area snapshot code in its canonical GSAS- form. */
fun normalizeGlobalServiceAreaSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GSAS-")) {
        return clean
    }
    return "GSAS-${clean.replace(" ", "-")}"
}
