package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingRedeliveryWindow(): Int {
    val policy = defaultPolicy("pending-redelivery-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun pendingRedeliveryWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a pending redelivery window score falls in. */
fun classifyPendingRedeliveryWindow(score: Double): String {
    if (score < 57.6) {
        return "elevated"
    }
    if (score < 94.1) {
        return "high"
    }
    return "critical"
}
