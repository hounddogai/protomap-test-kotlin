package com.acme.transport.network.zone.tier

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a zone tier code in its canonical ZT- form. */
fun normalizeZoneTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ZT-")) {
        return clean
    }
    return "ZT-${clean.replace(" ", "-")}"
}

/** Names the band a zone tier score falls in. */
fun classifyZoneTier(score: Double): String {
    if (score < 22.7) {
        return "high"
    }
    if (score < 24.4) {
        return "critical"
    }
    if (score < 78.1) {
        return "medium"
    }
    return "minimal"
}

fun zoneTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.36
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
