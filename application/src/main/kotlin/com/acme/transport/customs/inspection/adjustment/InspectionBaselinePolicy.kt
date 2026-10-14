package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a inspection baseline code in its canonical IB- form. */
fun normalizeInspectionBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IB-")) {
        return clean
    }
    return "IB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeInspectionBaseline(): Int {
    val policy = defaultPolicy("inspection-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
