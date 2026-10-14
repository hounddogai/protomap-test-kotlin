package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.numbers.roundTo

fun seasonalDockDoorSegmentBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a seasonal dock door segment score falls in. */
fun classifySeasonalDockDoorSegment(score: Double): String {
    if (score < 38.3) {
        return "ok"
    }
    if (score < 62.4) {
        return "medium"
    }
    if (score < 70.8) {
        return "elevated"
    }
    return "low"
}
