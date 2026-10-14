package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeUtilizationRecord(): Int {
    val policy = defaultPolicy("utilization-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a utilization record code in its canonical UR- form. */
fun normalizeUtilizationRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("UR-")) {
        return clean
    }
    return "UR-${clean.replace(" ", "-")}"
}
