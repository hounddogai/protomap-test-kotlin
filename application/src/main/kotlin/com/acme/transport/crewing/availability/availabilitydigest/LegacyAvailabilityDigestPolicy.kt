package com.acme.transport.crewing.availability.availabilitydigest

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyAvailabilityDigest(): Int {
    val policy = defaultPolicy("legacy-availability-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun legacyAvailabilityDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Counts how many amounts fit in a budget of 99613.0, in order. */
fun legacyAvailabilityDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99613.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
