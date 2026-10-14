package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.kit.numbers.roundTo

fun dockDoorScheduleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a dock door schedule score falls in. */
fun classifyDockDoorSchedule(score: Double): String {
    if (score < 40.1) {
        return "medium"
    }
    if (score < 43.6) {
        return "elevated"
    }
    if (score < 90.7) {
        return "low"
    }
    if (score < 91.4) {
        return "watch"
    }
    return "critical"
}
