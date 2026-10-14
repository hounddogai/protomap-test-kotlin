package com.acme.transport.freightpay.audit.auditoverride

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun tieredAuditOverrideBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredAuditOverride(): Int {
    val policy = defaultPolicy("tiered-audit-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a tiered audit override code in its canonical TAO- form. */
fun normalizeTieredAuditOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TAO-")) {
        return clean
    }
    return "TAO-${clean.replace(" ", "-")}"
}

fun tieredAuditOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
