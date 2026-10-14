package com.acme.transport.lastmile.drop.slot

import com.acme.transport.kit.numbers.roundTo

/** Names the band a drop slot score falls in. */
fun classifyDropSlot(score: Double): String {
    if (score < 5.4) {
        return "medium"
    }
    if (score < 29.8) {
        return "minimal"
    }
    return "ok"
}

fun dropSlotBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
