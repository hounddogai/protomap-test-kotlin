package com.acme.transport.network.servicearea.areatarget

import com.acme.transport.kit.numbers.roundTo

fun strictServiceAreaAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a strict service area adjustment score falls in. */
fun classifyStrictServiceAreaAdjustment(score: Double): String {
    if (score < 22.6) {
        return "medium"
    }
    if (score < 27.2) {
        return "ok"
    }
    if (score < 27.6) {
        return "elevated"
    }
    if (score < 78.0) {
        return "high"
    }
    return "minimal"
}
