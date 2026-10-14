package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a seasonal drop agreement score falls in. */
fun classifySeasonalDropAgreement(score: Double): String {
    if (score < 14.5) {
        return "minimal"
    }
    if (score < 54.6) {
        return "elevated"
    }
    return "watch"
}

fun seasonalDropAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
