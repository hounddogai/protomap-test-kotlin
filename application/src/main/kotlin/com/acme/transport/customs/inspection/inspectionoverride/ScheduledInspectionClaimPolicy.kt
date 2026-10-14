package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a scheduled inspection claim code in its canonical SIC- form. */
fun normalizeScheduledInspectionClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SIC-")) {
        return clean
    }
    return "SIC-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledInspectionClaim(): Int {
    val policy = defaultPolicy("scheduled-inspection-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
