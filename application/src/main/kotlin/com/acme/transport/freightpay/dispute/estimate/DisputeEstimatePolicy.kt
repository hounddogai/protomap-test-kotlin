package com.acme.transport.freightpay.dispute.estimate

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun disputeEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.77
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDisputeEstimate(): Int {
    val policy = defaultPolicy("dispute-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 38323.0, in order. */
fun disputeEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38323.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a dispute estimate score falls in. */
fun classifyDisputeEstimate(score: Double): String {
    if (score < 60.5) {
        return "high"
    }
    if (score < 91.0) {
        return "watch"
    }
    if (score < 100.0) {
        return "minimal"
    }
    return "ok"
}
