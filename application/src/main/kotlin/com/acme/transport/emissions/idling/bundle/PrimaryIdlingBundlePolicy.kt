package com.acme.transport.emissions.idling.bundle

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a primary idling bundle score falls in. */
fun classifyPrimaryIdlingBundle(score: Double): String {
    if (score < 4.9) {
        return "low"
    }
    if (score < 24.3) {
        return "minimal"
    }
    if (score < 68.3) {
        return "critical"
    }
    if (score < 75.5) {
        return "ok"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryIdlingBundle(): Int {
    val policy = defaultPolicy("primary-idling-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun primaryIdlingBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
