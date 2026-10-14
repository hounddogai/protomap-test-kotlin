package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a regional lease return cycle score falls in. */
fun classifyRegionalLeaseReturnCycle(score: Double): String {
    if (score < 16.3) {
        return "ok"
    }
    if (score < 30.6) {
        return "critical"
    }
    if (score < 34.3) {
        return "watch"
    }
    return "medium"
}

fun regionalLeaseReturnCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun regionalLeaseReturnCycleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalLeaseReturnCycle(): Int {
    val policy = defaultPolicy("regional-lease-return-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
