package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingDutyPeriodSnapshot(): Int {
    val policy = defaultPolicy("rolling-duty-period-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a rolling duty period snapshot score falls in. */
fun classifyRollingDutyPeriodSnapshot(score: Double): String {
    if (score < 39.5) {
        return "elevated"
    }
    if (score < 95.9) {
        return "high"
    }
    if (score < 97.4) {
        return "ok"
    }
    return "low"
}

fun rollingDutyPeriodSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
