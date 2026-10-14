package com.acme.transport.freightpay.audit.auditoverride

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalAuditBundle(): Int {
    val policy = defaultPolicy("global-audit-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a global audit bundle code in its canonical GAB- form. */
fun normalizeGlobalAuditBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GAB-")) {
        return clean
    }
    return "GAB-${clean.replace(" ", "-")}"
}
