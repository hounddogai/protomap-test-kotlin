package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.numbers.roundTo

fun lenientScorecardSlotBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a lenient scorecard slot score falls in. */
fun classifyLenientScorecardSlot(score: Double): String {
    if (score < 62.4) {
        return "ok"
    }
    if (score < 74.3) {
        return "watch"
    }
    return "elevated"
}
