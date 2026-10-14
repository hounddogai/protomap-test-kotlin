package com.acme.transport.network.zone.forecast

import com.acme.transport.kit.retry.defaultPolicy

fun adaptiveZoneRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveZoneRecord(): Int {
    val policy = defaultPolicy("adaptive-zone-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
