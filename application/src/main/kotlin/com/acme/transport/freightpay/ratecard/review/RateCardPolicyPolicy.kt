package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun rateCardPolicyBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a rate card policy score falls in. */
fun classifyRateCardPolicy(score: Double): String {
    if (score < 2.8) {
        return "minimal"
    }
    if (score < 80.9) {
        return "high"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 37341.0, in order. */
fun rateCardPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37341.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRateCardPolicy(): Int {
    val policy = defaultPolicy("rate-card-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
