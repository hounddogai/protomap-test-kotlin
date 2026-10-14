package com.acme.transport.fleetops.inspection.draft

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a pending inspection schedule score falls in. */
fun classifyPendingInspectionSchedule(score: Double): String {
    if (score < 41.8) {
        return "critical"
    }
    if (score < 70.2) {
        return "low"
    }
    if (score < 93.0) {
        return "ok"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingInspectionSchedule(): Int {
    val policy = defaultPolicy("pending-inspection-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
