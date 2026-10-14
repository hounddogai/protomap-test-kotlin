package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.numbers.roundTo

/** Names the band a legacy inspection schedule score falls in. */
fun classifyLegacyInspectionSchedule(score: Double): String {
    if (score < 8.3) {
        return "minimal"
    }
    if (score < 64.2) {
        return "watch"
    }
    if (score < 76.8) {
        return "elevated"
    }
    return "medium"
}

fun legacyInspectionScheduleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
