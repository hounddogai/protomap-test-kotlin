package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.kit.numbers.roundTo

fun weightedBrokerGroupBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a weighted broker group score falls in. */
fun classifyWeightedBrokerGroup(score: Double): String {
    if (score < 44.5) {
        return "medium"
    }
    if (score < 61.5) {
        return "watch"
    }
    if (score < 87.5) {
        return "critical"
    }
    if (score < 94.1) {
        return "low"
    }
    return "elevated"
}

fun weightedBrokerGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
