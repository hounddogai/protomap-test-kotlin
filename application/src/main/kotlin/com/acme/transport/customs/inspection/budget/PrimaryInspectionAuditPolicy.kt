package com.acme.transport.customs.inspection.budget

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryInspectionAudit(): Int {
    val policy = defaultPolicy("primary-inspection-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a primary inspection audit code in its canonical PIA- form. */
fun normalizePrimaryInspectionAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PIA-")) {
        return clean
    }
    return "PIA-${clean.replace(" ", "-")}"
}
