package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a weighted trailer mapping score falls in. */
fun classifyWeightedTrailerMapping(score: Double): String {
    if (score < 43.9) {
        return "critical"
    }
    if (score < 59.3) {
        return "elevated"
    }
    if (score < 66.5) {
        return "minimal"
    }
    if (score < 90.8) {
        return "ok"
    }
    return "watch"
}

fun weightedTrailerMappingBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun weightedTrailerMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.37
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a weighted trailer mapping code in its canonical WTM- form. */
fun normalizeWeightedTrailerMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WTM-")) {
        return clean
    }
    return "WTM-${clean.replace(" ", "-")}"
}
