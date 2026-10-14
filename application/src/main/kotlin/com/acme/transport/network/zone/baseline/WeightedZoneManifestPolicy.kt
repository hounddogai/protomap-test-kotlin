package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a weighted zone manifest score falls in. */
fun classifyWeightedZoneManifest(score: Double): String {
    if (score < 52.1) {
        return "minimal"
    }
    if (score < 55.6) {
        return "ok"
    }
    return "elevated"
}

fun weightedZoneManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.52
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun weightedZoneManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun weightedZoneManifestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
