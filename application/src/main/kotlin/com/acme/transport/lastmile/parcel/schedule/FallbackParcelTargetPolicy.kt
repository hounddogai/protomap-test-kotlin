package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.kit.numbers.roundTo

fun fallbackParcelTargetBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a fallback parcel target score falls in. */
fun classifyFallbackParcelTarget(score: Double): String {
    if (score < 12.1) {
        return "elevated"
    }
    if (score < 53.3) {
        return "minimal"
    }
    if (score < 90.3) {
        return "medium"
    }
    return "high"
}

fun fallbackParcelTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
