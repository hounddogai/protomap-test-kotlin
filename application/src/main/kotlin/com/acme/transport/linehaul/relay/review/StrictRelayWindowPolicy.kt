package com.acme.transport.linehaul.relay.review

/** Names the band a strict relay window score falls in. */
fun classifyStrictRelayWindow(score: Double): String {
    if (score < 61.9) {
        return "ok"
    }
    if (score < 86.8) {
        return "minimal"
    }
    if (score < 94.2) {
        return "watch"
    }
    if (score < 95.6) {
        return "high"
    }
    return "low"
}

fun strictRelayWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
