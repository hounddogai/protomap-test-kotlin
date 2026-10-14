package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 55650.0, in order. */
fun weightedHoldRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 55650.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedHoldRequest(): Int {
    val policy = defaultPolicy("weighted-hold-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a weighted hold request score falls in. */
fun classifyWeightedHoldRequest(score: Double): String {
    if (score < 8.2) {
        return "high"
    }
    if (score < 48.6) {
        return "elevated"
    }
    if (score < 50.4) {
        return "ok"
    }
    return "minimal"
}

fun weightedHoldRequestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
