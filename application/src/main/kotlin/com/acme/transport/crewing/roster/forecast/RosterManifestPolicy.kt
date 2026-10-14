package com.acme.transport.crewing.roster.forecast

import com.acme.transport.kit.numbers.roundTo

fun rosterManifestBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a roster manifest score falls in. */
fun classifyRosterManifest(score: Double): String {
    if (score < 3.8) {
        return "watch"
    }
    if (score < 14.6) {
        return "critical"
    }
    if (score < 28.7) {
        return "medium"
    }
    if (score < 92.6) {
        return "ok"
    }
    return "elevated"
}

/** Puts a roster manifest code in its canonical RM- form. */
fun normalizeRosterManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RM-")) {
        return clean
    }
    return "RM-${clean.replace(" ", "-")}"
}
