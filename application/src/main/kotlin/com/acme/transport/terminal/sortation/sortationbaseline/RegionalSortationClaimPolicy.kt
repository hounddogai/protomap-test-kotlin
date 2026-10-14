package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalSortationClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a regional sortation claim score falls in. */
fun classifyRegionalSortationClaim(score: Double): String {
    if (score < 28.2) {
        return "high"
    }
    if (score < 31.3) {
        return "minimal"
    }
    if (score < 81.2) {
        return "low"
    }
    if (score < 88.2) {
        return "critical"
    }
    return "ok"
}
