package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a locker allocation score falls in. */
fun classifyLockerAllocation(score: Double): String {
    if (score < 43.4) {
        return "high"
    }
    if (score < 73.1) {
        return "critical"
    }
    if (score < 77.4) {
        return "ok"
    }
    return "watch"
}

fun lockerAllocationBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLockerAllocation(): Int {
    val policy = defaultPolicy("locker-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun lockerAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
