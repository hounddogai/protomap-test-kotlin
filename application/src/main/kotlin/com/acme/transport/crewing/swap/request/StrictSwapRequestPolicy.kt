package com.acme.transport.crewing.swap.request

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 97537.0, in order. */
fun strictSwapRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97537.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictSwapRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.46
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictSwapRequest(): Int {
    val policy = defaultPolicy("strict-swap-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a strict swap request score falls in. */
fun classifyStrictSwapRequest(score: Double): String {
    if (score < 4.9) {
        return "low"
    }
    if (score < 56.1) {
        return "watch"
    }
    if (score < 64.3) {
        return "elevated"
    }
    if (score < 93.8) {
        return "high"
    }
    return "ok"
}
