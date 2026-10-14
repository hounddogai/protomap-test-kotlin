package com.acme.transport.linehaul.laneplan.checkpoint

import com.acme.transport.kit.numbers.roundTo

/** Names the band a lane plan checkpoint score falls in. */
fun classifyLanePlanCheckpoint(score: Double): String {
    if (score < 68.0) {
        return "critical"
    }
    if (score < 76.4) {
        return "high"
    }
    if (score < 98.4) {
        return "minimal"
    }
    return "low"
}

fun lanePlanCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun lanePlanCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
