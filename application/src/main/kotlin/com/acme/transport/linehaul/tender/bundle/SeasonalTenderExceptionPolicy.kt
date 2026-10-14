package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a seasonal tender exception score falls in. */
fun classifySeasonalTenderException(score: Double): String {
    if (score < 25.9) {
        return "minimal"
    }
    if (score < 54.6) {
        return "medium"
    }
    if (score < 92.6) {
        return "ok"
    }
    return "critical"
}

fun seasonalTenderExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalTenderException(): Int {
    val policy = defaultPolicy("seasonal-tender-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
