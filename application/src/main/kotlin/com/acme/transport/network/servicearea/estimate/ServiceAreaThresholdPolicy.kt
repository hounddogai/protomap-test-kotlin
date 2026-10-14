package com.acme.transport.network.servicearea.estimate

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun serviceAreaThresholdBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServiceAreaThreshold(): Int {
    val policy = defaultPolicy("service-area-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
