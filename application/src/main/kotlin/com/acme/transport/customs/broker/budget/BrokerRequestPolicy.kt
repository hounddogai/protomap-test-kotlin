package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.numbers.roundTo

fun brokerRequestBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a broker request score falls in. */
fun classifyBrokerRequest(score: Double): String {
    if (score < 51.3) {
        return "high"
    }
    if (score < 72.0) {
        return "watch"
    }
    return "medium"
}
