package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a manual audit policy code in its canonical MAP- form. */
fun normalizeManualAuditPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MAP-")) {
        return clean
    }
    return "MAP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualAuditPolicy(): Int {
    val policy = defaultPolicy("manual-audit-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
