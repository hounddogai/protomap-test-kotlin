package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun strictCoachingEntryBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a strict coaching entry score falls in. */
fun classifyStrictCoachingEntry(score: Double): String {
    if (score < 6.6) {
        return "high"
    }
    if (score < 62.9) {
        return "minimal"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 30911.0, in order. */
fun strictCoachingEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30911.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictCoachingEntry(): Int {
    val policy = defaultPolicy("strict-coaching-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
