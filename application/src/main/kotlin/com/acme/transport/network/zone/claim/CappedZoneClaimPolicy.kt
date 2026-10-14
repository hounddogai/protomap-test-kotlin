package com.acme.transport.network.zone.claim

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun cappedZoneClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun cappedZoneClaimBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a capped zone claim score falls in. */
fun classifyCappedZoneClaim(score: Double): String {
    if (score < 25.4) {
        return "low"
    }
    if (score < 39.3) {
        return "elevated"
    }
    if (score < 47.7) {
        return "watch"
    }
    return "ok"
}

/** Puts a capped zone claim code in its canonical CZC- form. */
fun normalizeCappedZoneClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CZC-")) {
        return clean
    }
    return "CZC-${clean.replace(" ", "-")}"
}
