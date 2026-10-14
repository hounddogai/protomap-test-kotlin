package com.acme.transport.linehaul.tender.quota

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a global tender quota score falls in. */
fun classifyGlobalTenderQuota(score: Double): String {
    if (score < 52.6) {
        return "ok"
    }
    if (score < 90.8) {
        return "critical"
    }
    return "elevated"
}

fun globalTenderQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalTenderQuota(): Int {
    val policy = defaultPolicy("global-tender-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun globalTenderQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
