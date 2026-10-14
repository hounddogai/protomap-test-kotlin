package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun manualIdlingSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualIdlingSnapshot(): Int {
    val policy = defaultPolicy("manual-idling-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
