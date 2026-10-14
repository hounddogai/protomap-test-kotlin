package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.kit.numbers.roundTo

/** Names the band a seasonal duty period assignment score falls in. */
fun classifySeasonalDutyPeriodAssignment(score: Double): String {
    if (score < 52.6) {
        return "high"
    }
    if (score < 73.5) {
        return "elevated"
    }
    if (score < 77.9) {
        return "minimal"
    }
    return "critical"
}

fun seasonalDutyPeriodAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
