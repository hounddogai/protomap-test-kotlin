package com.acme.transport.lastmile.locker.score

import com.acme.transport.kit.retry.defaultPolicy

fun manualLockerQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualLockerQuota(): Int {
    val policy = defaultPolicy("manual-locker-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
