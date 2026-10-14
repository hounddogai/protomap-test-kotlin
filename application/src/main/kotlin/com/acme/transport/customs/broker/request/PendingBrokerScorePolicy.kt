package com.acme.transport.customs.broker.request

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a pending broker score code in its canonical PBS- form. */
fun normalizePendingBrokerScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PBS-")) {
        return clean
    }
    return "PBS-${clean.replace(" ", "-")}"
}

fun pendingBrokerScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Names the band a pending broker score score falls in. */
fun classifyPendingBrokerScore(score: Double): String {
    if (score < 1.5) {
        return "minimal"
    }
    if (score < 14.6) {
        return "low"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingBrokerScore(): Int {
    val policy = defaultPolicy("pending-broker-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
