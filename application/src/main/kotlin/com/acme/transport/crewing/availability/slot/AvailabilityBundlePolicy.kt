package com.acme.transport.crewing.availability.slot

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun availabilityBundleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a availability bundle score falls in. */
fun classifyAvailabilityBundle(score: Double): String {
    if (score < 8.5) {
        return "medium"
    }
    if (score < 30.5) {
        return "minimal"
    }
    return "ok"
}

fun availabilityBundleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
