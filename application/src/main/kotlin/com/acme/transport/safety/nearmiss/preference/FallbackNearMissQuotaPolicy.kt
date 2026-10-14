package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.retry.defaultPolicy

fun fallbackNearMissQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackNearMissQuota(): Int {
    val policy = defaultPolicy("fallback-near-miss-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
