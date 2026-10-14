package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a violation audit score falls in. */
fun classifyViolationAudit(score: Double): String {
    if (score < 32.1) {
        return "high"
    }
    if (score < 59.1) {
        return "minimal"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeViolationAudit(): Int {
    val policy = defaultPolicy("violation-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a violation audit code in its canonical VA- form. */
fun normalizeViolationAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("VA-")) {
        return clean
    }
    return "VA-${clean.replace(" ", "-")}"
}
