package com.acme.transport.lastmile.redelivery.redeliveryclaim

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a redelivery rule code in its canonical RR- form. */
fun normalizeRedeliveryRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RR-")) {
        return clean
    }
    return "RR-${clean.replace(" ", "-")}"
}

/** Names the band a redelivery rule score falls in. */
fun classifyRedeliveryRule(score: Double): String {
    if (score < 31.3) {
        return "high"
    }
    if (score < 58.9) {
        return "ok"
    }
    if (score < 93.5) {
        return "elevated"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRedeliveryRule(): Int {
    val policy = defaultPolicy("redelivery-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
