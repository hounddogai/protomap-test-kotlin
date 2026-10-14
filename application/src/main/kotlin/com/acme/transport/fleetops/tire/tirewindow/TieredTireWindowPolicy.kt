package com.acme.transport.fleetops.tire.tirewindow

import com.acme.transport.kit.numbers.roundTo

fun tieredTireWindowBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a tiered tire window score falls in. */
fun classifyTieredTireWindow(score: Double): String {
    if (score < 30.9) {
        return "high"
    }
    if (score < 71.4) {
        return "elevated"
    }
    if (score < 89.2) {
        return "medium"
    }
    if (score < 91.5) {
        return "low"
    }
    return "ok"
}
