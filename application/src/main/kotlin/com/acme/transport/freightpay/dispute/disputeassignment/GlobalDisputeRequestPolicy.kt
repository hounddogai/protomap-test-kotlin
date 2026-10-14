package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalDisputeRequest(): Int {
    val policy = defaultPolicy("global-dispute-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a global dispute request score falls in. */
fun classifyGlobalDisputeRequest(score: Double): String {
    if (score < 2.7) {
        return "low"
    }
    if (score < 54.2) {
        return "ok"
    }
    if (score < 71.0) {
        return "critical"
    }
    return "watch"
}

fun globalDisputeRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.7
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun globalDisputeRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
