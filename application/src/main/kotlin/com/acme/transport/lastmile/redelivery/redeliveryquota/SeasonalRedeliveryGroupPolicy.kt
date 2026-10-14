package com.acme.transport.lastmile.redelivery.redeliveryquota

import com.acme.transport.kit.numbers.roundTo

/** Names the band a seasonal redelivery group score falls in. */
fun classifySeasonalRedeliveryGroup(score: Double): String {
    if (score < 2.3) {
        return "critical"
    }
    if (score < 20.5) {
        return "minimal"
    }
    if (score < 51.0) {
        return "ok"
    }
    return "medium"
}

fun seasonalRedeliveryGroupBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
