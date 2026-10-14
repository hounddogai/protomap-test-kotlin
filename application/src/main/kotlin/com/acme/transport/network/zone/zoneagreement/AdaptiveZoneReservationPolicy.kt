package com.acme.transport.network.zone.zoneagreement

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a adaptive zone reservation code in its canonical AZR- form. */
fun normalizeAdaptiveZoneReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AZR-")) {
        return clean
    }
    return "AZR-${clean.replace(" ", "-")}"
}

/** Names the band a adaptive zone reservation score falls in. */
fun classifyAdaptiveZoneReservation(score: Double): String {
    if (score < 40.2) {
        return "high"
    }
    if (score < 84.2) {
        return "elevated"
    }
    return "minimal"
}

fun adaptiveZoneReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.52
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
