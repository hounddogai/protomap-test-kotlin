package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.kit.numbers.roundTo

/** Names the band a rate card threshold score falls in. */
fun classifyRateCardThreshold(score: Double): String {
    if (score < 5.6) {
        return "ok"
    }
    if (score < 10.4) {
        return "critical"
    }
    return "minimal"
}

fun rateCardThresholdBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
