package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingInspectionRequest(): Int {
    val policy = defaultPolicy("pending-inspection-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a pending inspection request code in its canonical PIR- form. */
fun normalizePendingInspectionRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PIR-")) {
        return clean
    }
    return "PIR-${clean.replace(" ", "-")}"
}

fun pendingInspectionRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
