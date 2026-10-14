package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun brokerClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a broker claim score falls in. */
fun classifyBrokerClaim(score: Double): String {
    if (score < 5.7) {
        return "ok"
    }
    if (score < 85.0) {
        return "minimal"
    }
    if (score < 95.9) {
        return "low"
    }
    if (score < 98.2) {
        return "critical"
    }
    return "medium"
}

fun brokerClaimBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBrokerClaim(): Int {
    val policy = defaultPolicy("broker-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
