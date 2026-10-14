package com.acme.transport.fleetops.leasereturn.segment

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun leaseReturnSegmentBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun leaseReturnSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a lease return segment score falls in. */
fun classifyLeaseReturnSegment(score: Double): String {
    if (score < 19.2) {
        return "ok"
    }
    if (score < 48.3) {
        return "minimal"
    }
    if (score < 49.4) {
        return "medium"
    }
    if (score < 99.2) {
        return "watch"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLeaseReturnSegment(): Int {
    val policy = defaultPolicy("lease-return-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
