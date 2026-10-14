package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun strictNearMissPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a strict near miss policy score falls in. */
fun classifyStrictNearMissPolicy(score: Double): String {
    if (score < 21.2) {
        return "medium"
    }
    if (score < 29.1) {
        return "critical"
    }
    if (score < 63.3) {
        return "ok"
    }
    return "minimal"
}

fun strictNearMissPolicyBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
