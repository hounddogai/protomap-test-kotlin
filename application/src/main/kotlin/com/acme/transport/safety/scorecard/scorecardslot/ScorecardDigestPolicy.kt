package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun scorecardDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a scorecard digest score falls in. */
fun classifyScorecardDigest(score: Double): String {
    if (score < 59.6) {
        return "high"
    }
    if (score < 74.5) {
        return "watch"
    }
    if (score < 97.7) {
        return "elevated"
    }
    if (score < 98.5) {
        return "medium"
    }
    return "minimal"
}
