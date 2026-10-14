package com.acme.transport.linehaul.relay.digest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveRelayDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun adaptiveRelayDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.03
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a adaptive relay digest score falls in. */
fun classifyAdaptiveRelayDigest(score: Double): String {
    if (score < 16.4) {
        return "low"
    }
    if (score < 46.6) {
        return "minimal"
    }
    return "medium"
}
