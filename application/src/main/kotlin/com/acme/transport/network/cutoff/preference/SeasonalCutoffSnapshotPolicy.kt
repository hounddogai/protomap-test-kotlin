package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.numbers.roundTo

/** Names the band a seasonal cutoff snapshot score falls in. */
fun classifySeasonalCutoffSnapshot(score: Double): String {
    if (score < 13.7) {
        return "minimal"
    }
    if (score < 42.1) {
        return "critical"
    }
    if (score < 47.4) {
        return "low"
    }
    if (score < 67.7) {
        return "high"
    }
    return "medium"
}

/** Puts a seasonal cutoff snapshot code in its canonical SCS- form. */
fun normalizeSeasonalCutoffSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCS-")) {
        return clean
    }
    return "SCS-${clean.replace(" ", "-")}"
}

fun seasonalCutoffSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
