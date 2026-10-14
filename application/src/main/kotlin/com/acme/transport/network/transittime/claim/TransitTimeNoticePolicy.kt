package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a transit time notice score falls in. */
fun classifyTransitTimeNotice(score: Double): String {
    if (score < 17.7) {
        return "medium"
    }
    if (score < 42.0) {
        return "elevated"
    }
    if (score < 46.5) {
        return "high"
    }
    if (score < 47.4) {
        return "watch"
    }
    return "low"
}

fun transitTimeNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.16
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a transit time notice code in its canonical TTN- form. */
fun normalizeTransitTimeNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTN-")) {
        return clean
    }
    return "TTN-${clean.replace(" ", "-")}"
}
