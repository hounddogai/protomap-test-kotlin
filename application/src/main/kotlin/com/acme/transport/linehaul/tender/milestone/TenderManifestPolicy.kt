package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun tenderManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a tender manifest score falls in. */
fun classifyTenderManifest(score: Double): String {
    if (score < 30.6) {
        return "minimal"
    }
    if (score < 57.2) {
        return "critical"
    }
    if (score < 74.3) {
        return "ok"
    }
    if (score < 76.5) {
        return "watch"
    }
    return "elevated"
}

fun tenderManifestBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
