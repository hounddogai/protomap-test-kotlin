package com.acme.transport.linehaul.trailer.score

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun cappedTrailerRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a capped trailer rule score falls in. */
fun classifyCappedTrailerRule(score: Double): String {
    if (score < 1.1) {
        return "high"
    }
    if (score < 8.8) {
        return "minimal"
    }
    if (score < 30.1) {
        return "watch"
    }
    if (score < 89.0) {
        return "medium"
    }
    return "ok"
}
