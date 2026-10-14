package com.acme.transport.fleetops.leasereturn.bundle

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLeaseReturnOverride(): Int {
    val policy = defaultPolicy("lease-return-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a lease return override score falls in. */
fun classifyLeaseReturnOverride(score: Double): String {
    if (score < 29.7) {
        return "high"
    }
    if (score < 44.5) {
        return "minimal"
    }
    if (score < 99.5) {
        return "watch"
    }
    return "critical"
}

fun leaseReturnOverrideBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
