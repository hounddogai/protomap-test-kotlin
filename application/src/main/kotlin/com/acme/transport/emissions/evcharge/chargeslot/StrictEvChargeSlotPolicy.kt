package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.numbers.roundTo

/** Names the band a strict ev charge slot score falls in. */
fun classifyStrictEvChargeSlot(score: Double): String {
    if (score < 36.3) {
        return "ok"
    }
    if (score < 68.5) {
        return "minimal"
    }
    return "high"
}

fun strictEvChargeSlotBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun strictEvChargeSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
