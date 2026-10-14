package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRestBreakVariance(): Int {
    val policy = defaultPolicy("rest-break-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 29367.0, in order. */
fun restBreakVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 29367.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a rest break variance score falls in. */
fun classifyRestBreakVariance(score: Double): String {
    if (score < 5.8) {
        return "ok"
    }
    if (score < 20.8) {
        return "elevated"
    }
    if (score < 28.4) {
        return "medium"
    }
    if (score < 59.3) {
        return "watch"
    }
    return "high"
}

fun restBreakVarianceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
