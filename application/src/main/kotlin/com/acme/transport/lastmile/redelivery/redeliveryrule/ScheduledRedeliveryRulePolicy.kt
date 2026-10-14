package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a scheduled redelivery rule code in its canonical SRR- form. */
fun normalizeScheduledRedeliveryRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRR-")) {
        return clean
    }
    return "SRR-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledRedeliveryRule(): Int {
    val policy = defaultPolicy("scheduled-redelivery-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
