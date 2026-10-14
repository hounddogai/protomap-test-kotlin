package com.acme.transport.crewing.availability.score

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 94603.0, in order. */
fun lenientAvailabilityReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94603.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientAvailabilityReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientAvailabilityReview(): Int {
    val policy = defaultPolicy("lenient-availability-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
