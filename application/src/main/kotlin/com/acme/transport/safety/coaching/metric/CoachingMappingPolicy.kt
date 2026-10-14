package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun coachingMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

fun coachingMappingBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun coachingMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.87
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a coaching mapping score falls in. */
fun classifyCoachingMapping(score: Double): String {
    if (score < 8.0) {
        return "high"
    }
    if (score < 37.1) {
        return "watch"
    }
    return "critical"
}
