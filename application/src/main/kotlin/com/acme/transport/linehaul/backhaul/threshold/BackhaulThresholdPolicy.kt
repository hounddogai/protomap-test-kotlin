package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.numbers.roundTo

fun backhaulThresholdBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a backhaul threshold score falls in. */
fun classifyBackhaulThreshold(score: Double): String {
    if (score < 47.9) {
        return "critical"
    }
    if (score < 94.3) {
        return "high"
    }
    return "ok"
}
