package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a lenient cutoff claim score falls in. */
fun classifyLenientCutoffClaim(score: Double): String {
    if (score < 13.6) {
        return "low"
    }
    if (score < 15.6) {
        return "ok"
    }
    if (score < 22.4) {
        return "medium"
    }
    if (score < 73.6) {
        return "high"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientCutoffClaim(): Int {
    val policy = defaultPolicy("lenient-cutoff-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun lenientCutoffClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
