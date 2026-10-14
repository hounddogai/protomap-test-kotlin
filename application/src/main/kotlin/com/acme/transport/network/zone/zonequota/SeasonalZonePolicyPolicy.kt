package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.numbers.roundTo

/** Names the band a seasonal zone policy score falls in. */
fun classifySeasonalZonePolicy(score: Double): String {
    if (score < 20.7) {
        return "ok"
    }
    if (score < 95.7) {
        return "medium"
    }
    return "low"
}

fun seasonalZonePolicyBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun seasonalZonePolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
