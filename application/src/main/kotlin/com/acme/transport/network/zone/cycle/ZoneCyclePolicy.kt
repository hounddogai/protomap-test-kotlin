package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun zoneCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeZoneCycle(): Int {
    val policy = defaultPolicy("zone-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun zoneCycleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
