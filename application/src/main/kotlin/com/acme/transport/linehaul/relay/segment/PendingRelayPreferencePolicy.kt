package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.numbers.roundTo

fun pendingRelayPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a pending relay preference score falls in. */
fun classifyPendingRelayPreference(score: Double): String {
    if (score < 14.3) {
        return "elevated"
    }
    if (score < 34.9) {
        return "medium"
    }
    if (score < 70.6) {
        return "watch"
    }
    return "minimal"
}
