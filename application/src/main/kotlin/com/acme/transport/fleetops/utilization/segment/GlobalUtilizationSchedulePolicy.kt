package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalUtilizationSchedule(): Int {
    val policy = defaultPolicy("global-utilization-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a global utilization schedule code in its canonical GUS- form. */
fun normalizeGlobalUtilizationScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GUS-")) {
        return clean
    }
    return "GUS-${clean.replace(" ", "-")}"
}
