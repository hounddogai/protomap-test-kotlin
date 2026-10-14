package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a inspection policy code in its canonical IP- form. */
fun normalizeInspectionPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IP-")) {
        return clean
    }
    return "IP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeInspectionPolicy(): Int {
    val policy = defaultPolicy("inspection-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
