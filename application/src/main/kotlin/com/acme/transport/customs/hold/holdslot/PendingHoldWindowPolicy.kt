package com.acme.transport.customs.hold.holdslot

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a pending hold window score falls in. */
fun classifyPendingHoldWindow(score: Double): String {
    if (score < 47.6) {
        return "watch"
    }
    if (score < 48.3) {
        return "medium"
    }
    return "critical"
}

fun pendingHoldWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingHoldWindow(): Int {
    val policy = defaultPolicy("pending-hold-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
