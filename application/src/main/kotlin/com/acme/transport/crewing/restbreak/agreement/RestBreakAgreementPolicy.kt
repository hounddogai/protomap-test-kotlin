package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRestBreakAgreement(): Int {
    val policy = defaultPolicy("rest-break-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a rest break agreement score falls in. */
fun classifyRestBreakAgreement(score: Double): String {
    if (score < 22.8) {
        return "medium"
    }
    if (score < 87.1) {
        return "elevated"
    }
    if (score < 87.2) {
        return "critical"
    }
    return "ok"
}

fun restBreakAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
