package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a dispute tier score falls in. */
fun classifyDisputeTier(score: Double): String {
    if (score < 38.3) {
        return "low"
    }
    if (score < 43.1) {
        return "critical"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDisputeTier(): Int {
    val policy = defaultPolicy("dispute-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun disputeTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun disputeTierBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
