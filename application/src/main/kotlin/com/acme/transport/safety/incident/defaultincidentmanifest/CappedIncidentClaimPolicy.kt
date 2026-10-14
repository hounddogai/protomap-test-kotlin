package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun cappedIncidentClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a capped incident claim score falls in. */
fun classifyCappedIncidentClaim(score: Double): String {
    if (score < 5.8) {
        return "low"
    }
    if (score < 22.0) {
        return "watch"
    }
    if (score < 99.3) {
        return "ok"
    }
    return "critical"
}

fun cappedIncidentClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun cappedIncidentClaimBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
