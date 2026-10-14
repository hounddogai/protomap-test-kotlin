package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a near miss segment score falls in. */
fun classifyNearMissSegment(score: Double): String {
    if (score < 37.9) {
        return "ok"
    }
    if (score < 57.0) {
        return "critical"
    }
    if (score < 98.9) {
        return "watch"
    }
    return "elevated"
}

fun nearMissSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

fun nearMissSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
