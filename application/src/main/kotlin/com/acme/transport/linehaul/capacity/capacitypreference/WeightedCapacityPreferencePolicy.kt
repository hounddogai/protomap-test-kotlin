package com.acme.transport.linehaul.capacity.capacitypreference

import com.acme.transport.kit.numbers.roundTo

fun weightedCapacityPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a weighted capacity preference score falls in. */
fun classifyWeightedCapacityPreference(score: Double): String {
    if (score < 17.3) {
        return "high"
    }
    if (score < 21.9) {
        return "critical"
    }
    if (score < 65.6) {
        return "ok"
    }
    return "elevated"
}

fun weightedCapacityPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
