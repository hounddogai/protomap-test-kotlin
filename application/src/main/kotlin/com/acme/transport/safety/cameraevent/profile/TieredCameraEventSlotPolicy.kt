package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.numbers.roundTo

fun tieredCameraEventSlotBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a tiered camera event slot score falls in. */
fun classifyTieredCameraEventSlot(score: Double): String {
    if (score < 45.1) {
        return "elevated"
    }
    if (score < 49.6) {
        return "medium"
    }
    if (score < 69.5) {
        return "watch"
    }
    return "critical"
}

fun tieredCameraEventSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
