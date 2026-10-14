package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.numbers.roundTo

fun evChargeMappingBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a ev charge mapping score falls in. */
fun classifyEvChargeMapping(score: Double): String {
    if (score < 26.6) {
        return "minimal"
    }
    if (score < 45.9) {
        return "ok"
    }
    if (score < 84.7) {
        return "medium"
    }
    return "elevated"
}
