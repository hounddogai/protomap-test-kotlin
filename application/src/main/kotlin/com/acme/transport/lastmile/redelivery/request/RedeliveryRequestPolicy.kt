package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.retry.defaultPolicy

fun redeliveryRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRedeliveryRequest(): Int {
    val policy = defaultPolicy("redelivery-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
