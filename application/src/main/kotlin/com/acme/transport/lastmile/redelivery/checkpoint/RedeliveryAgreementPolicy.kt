package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRedeliveryAgreement(): Int {
    val policy = defaultPolicy("redelivery-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a redelivery agreement code in its canonical RA- form. */
fun normalizeRedeliveryAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RA-")) {
        return clean
    }
    return "RA-${clean.replace(" ", "-")}"
}

/** Names the band a redelivery agreement score falls in. */
fun classifyRedeliveryAgreement(score: Double): String {
    if (score < 73.8) {
        return "high"
    }
    if (score < 94.7) {
        return "medium"
    }
    return "critical"
}
