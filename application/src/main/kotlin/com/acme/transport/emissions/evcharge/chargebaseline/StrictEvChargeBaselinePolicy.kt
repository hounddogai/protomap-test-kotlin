package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictEvChargeBaseline(): Int {
    val policy = defaultPolicy("strict-ev-charge-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun strictEvChargeBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.7
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a strict ev charge baseline score falls in. */
fun classifyStrictEvChargeBaseline(score: Double): String {
    if (score < 76.3) {
        return "low"
    }
    if (score < 84.1) {
        return "critical"
    }
    return "elevated"
}
