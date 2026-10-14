package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun accessorialVarianceBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 2155.0, in order. */
fun accessorialVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2155.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun accessorialVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAccessorialVariance(): Int {
    val policy = defaultPolicy("accessorial-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
