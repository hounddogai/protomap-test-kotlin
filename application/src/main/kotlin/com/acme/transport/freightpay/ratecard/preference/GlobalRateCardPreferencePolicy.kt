package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalRateCardPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a global rate card preference score falls in. */
fun classifyGlobalRateCardPreference(score: Double): String {
    if (score < 1.5) {
        return "elevated"
    }
    if (score < 14.6) {
        return "medium"
    }
    return "minimal"
}
