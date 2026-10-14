package com.acme.transport.customs.broker.baseline

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a broker baseline score falls in. */
fun classifyBrokerBaseline(score: Double): String {
    if (score < 3.0) {
        return "watch"
    }
    if (score < 14.6) {
        return "elevated"
    }
    if (score < 56.3) {
        return "ok"
    }
    if (score < 66.8) {
        return "low"
    }
    return "high"
}

fun brokerBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBrokerBaseline(): Int {
    val policy = defaultPolicy("broker-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
