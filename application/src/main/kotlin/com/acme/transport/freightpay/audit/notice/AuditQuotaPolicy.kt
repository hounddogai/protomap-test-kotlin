package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAuditQuota(): Int {
    val policy = defaultPolicy("audit-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a audit quota score falls in. */
fun classifyAuditQuota(score: Double): String {
    if (score < 50.3) {
        return "high"
    }
    if (score < 69.1) {
        return "minimal"
    }
    if (score < 83.7) {
        return "ok"
    }
    return "low"
}
