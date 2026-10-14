package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a legacy camera event claim code in its canonical LCEC- form. */
fun normalizeLegacyCameraEventClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCEC-")) {
        return clean
    }
    return "LCEC-${clean.replace(" ", "-")}"
}

fun legacyCameraEventClaimBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a legacy camera event claim score falls in. */
fun classifyLegacyCameraEventClaim(score: Double): String {
    if (score < 19.9) {
        return "medium"
    }
    if (score < 20.6) {
        return "elevated"
    }
    if (score < 39.8) {
        return "low"
    }
    if (score < 98.1) {
        return "ok"
    }
    return "minimal"
}

fun legacyCameraEventClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.25
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
