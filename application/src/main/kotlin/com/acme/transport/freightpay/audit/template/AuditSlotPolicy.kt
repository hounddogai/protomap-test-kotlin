package com.acme.transport.freightpay.audit.template

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAuditSlot(): Int {
    val policy = defaultPolicy("audit-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a audit slot score falls in. */
fun classifyAuditSlot(score: Double): String {
    if (score < 50.3) {
        return "low"
    }
    if (score < 62.2) {
        return "ok"
    }
    return "high"
}
