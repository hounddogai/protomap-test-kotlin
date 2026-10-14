package com.acme.transport.customs.clearance.clearancerequest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun primaryClearanceRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.98
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a primary clearance request score falls in. */
fun classifyPrimaryClearanceRequest(score: Double): String {
    if (score < 36.7) {
        return "minimal"
    }
    if (score < 39.6) {
        return "high"
    }
    if (score < 54.8) {
        return "ok"
    }
    if (score < 72.6) {
        return "low"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryClearanceRequest(): Int {
    val policy = defaultPolicy("primary-clearance-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun primaryClearanceRequestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
