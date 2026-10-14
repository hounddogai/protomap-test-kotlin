package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun seasonalTrailerYardPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a seasonal trailer yard policy score falls in. */
fun classifySeasonalTrailerYardPolicy(score: Double): String {
    if (score < 6.5) {
        return "watch"
    }
    if (score < 98.2) {
        return "minimal"
    }
    return "medium"
}
