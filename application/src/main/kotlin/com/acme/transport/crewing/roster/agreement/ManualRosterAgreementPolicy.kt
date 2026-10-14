package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a manual roster agreement score falls in. */
fun classifyManualRosterAgreement(score: Double): String {
    if (score < 12.2) {
        return "minimal"
    }
    if (score < 20.7) {
        return "ok"
    }
    if (score < 32.0) {
        return "watch"
    }
    if (score < 82.4) {
        return "elevated"
    }
    return "medium"
}

fun manualRosterAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.77
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
