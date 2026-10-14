package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun strictDisputeQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictDisputeQuota(): Int {
    val policy = defaultPolicy("strict-dispute-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a strict dispute quota score falls in. */
fun classifyStrictDisputeQuota(score: Double): String {
    if (score < 24.6) {
        return "low"
    }
    if (score < 45.0) {
        return "ok"
    }
    if (score < 66.8) {
        return "high"
    }
    if (score < 88.2) {
        return "critical"
    }
    return "medium"
}
