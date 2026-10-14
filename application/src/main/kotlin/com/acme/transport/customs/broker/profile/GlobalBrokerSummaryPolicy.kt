package com.acme.transport.customs.broker.profile

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun globalBrokerSummaryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalBrokerSummary(): Int {
    val policy = defaultPolicy("global-broker-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a global broker summary code in its canonical GBS- form. */
fun normalizeGlobalBrokerSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GBS-")) {
        return clean
    }
    return "GBS-${clean.replace(" ", "-")}"
}
