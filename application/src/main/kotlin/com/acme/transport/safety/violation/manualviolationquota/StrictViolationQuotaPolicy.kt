package com.acme.transport.safety.violation.manualviolationquota

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a strict violation quota code in its canonical SVQ- form. */
fun normalizeStrictViolationQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SVQ-")) {
        return clean
    }
    return "SVQ-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictViolationQuota(): Int {
    val policy = defaultPolicy("strict-violation-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
