package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.retry.defaultPolicy

fun tieredClearanceQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredClearanceQuota(): Int {
    val policy = defaultPolicy("tiered-clearance-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
