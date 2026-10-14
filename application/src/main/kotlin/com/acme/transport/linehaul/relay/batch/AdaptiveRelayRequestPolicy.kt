package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.numbers.roundTo

fun adaptiveRelayRequestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a adaptive relay request score falls in. */
fun classifyAdaptiveRelayRequest(score: Double): String {
    if (score < 53.6) {
        return "minimal"
    }
    if (score < 67.1) {
        return "watch"
    }
    if (score < 75.9) {
        return "critical"
    }
    return "medium"
}

/** Puts a adaptive relay request code in its canonical ARR- form. */
fun normalizeAdaptiveRelayRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ARR-")) {
        return clean
    }
    return "ARR-${clean.replace(" ", "-")}"
}
