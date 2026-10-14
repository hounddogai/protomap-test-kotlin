package com.acme.transport.linehaul.capacity.audit

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a capacity agreement code in its canonical CA- form. */
fun normalizeCapacityAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CA-")) {
        return clean
    }
    return "CA-${clean.replace(" ", "-")}"
}

fun capacityAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCapacityAgreement(): Int {
    val policy = defaultPolicy("capacity-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
