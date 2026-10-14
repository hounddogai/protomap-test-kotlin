package com.acme.transport.customs.inspection.slot

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun regionalInspectionScheduleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a regional inspection schedule score falls in. */
fun classifyRegionalInspectionSchedule(score: Double): String {
    if (score < 26.8) {
        return "ok"
    }
    if (score < 53.7) {
        return "minimal"
    }
    if (score < 58.0) {
        return "elevated"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalInspectionSchedule(): Int {
    val policy = defaultPolicy("regional-inspection-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
