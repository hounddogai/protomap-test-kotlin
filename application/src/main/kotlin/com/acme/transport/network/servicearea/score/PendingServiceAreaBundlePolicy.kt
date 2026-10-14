package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingServiceAreaBundle(): Int {
    val policy = defaultPolicy("pending-service-area-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun pendingServiceAreaBundleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 56471.0, in order. */
fun pendingServiceAreaBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 56471.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a pending service area bundle score falls in. */
fun classifyPendingServiceAreaBundle(score: Double): String {
    if (score < 69.7) {
        return "ok"
    }
    if (score < 90.8) {
        return "elevated"
    }
    if (score < 97.7) {
        return "minimal"
    }
    return "medium"
}
