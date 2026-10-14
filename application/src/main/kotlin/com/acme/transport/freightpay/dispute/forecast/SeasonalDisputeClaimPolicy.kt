package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalDisputeClaim(): Int {
    val policy = defaultPolicy("seasonal-dispute-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a seasonal dispute claim score falls in. */
fun classifySeasonalDisputeClaim(score: Double): String {
    if (score < 1.8) {
        return "watch"
    }
    if (score < 22.9) {
        return "medium"
    }
    return "minimal"
}

fun seasonalDisputeClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun seasonalDisputeClaimBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
