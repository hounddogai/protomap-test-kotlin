package com.acme.transport.lastmile.attempt.attemptpolicy

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a attempt metric code in its canonical AM- form. */
fun normalizeAttemptMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AM-")) {
        return clean
    }
    return "AM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAttemptMetric(): Int {
    val policy = defaultPolicy("attempt-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
