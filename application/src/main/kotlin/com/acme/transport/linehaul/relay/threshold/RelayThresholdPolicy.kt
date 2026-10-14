package com.acme.transport.linehaul.relay.threshold

/** Puts a relay threshold code in its canonical RT- form. */
fun normalizeRelayThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RT-")) {
        return clean
    }
    return "RT-${clean.replace(" ", "-")}"
}

fun relayThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a relay threshold score falls in. */
fun classifyRelayThreshold(score: Double): String {
    if (score < 18.0) {
        return "medium"
    }
    if (score < 61.6) {
        return "low"
    }
    return "elevated"
}
