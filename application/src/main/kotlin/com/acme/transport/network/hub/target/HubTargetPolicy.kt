package com.acme.transport.network.hub.target

import com.acme.transport.kit.numbers.roundTo

fun hubTargetBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a hub target score falls in. */
fun classifyHubTarget(score: Double): String {
    if (score < 80.0) {
        return "critical"
    }
    if (score < 85.9) {
        return "watch"
    }
    if (score < 88.9) {
        return "minimal"
    }
    return "high"
}

/** Puts a hub target code in its canonical HT- form. */
fun normalizeHubTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HT-")) {
        return clean
    }
    return "HT-${clean.replace(" ", "-")}"
}
