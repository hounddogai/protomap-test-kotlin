package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a backhaul bundle score falls in. */
fun classifyBackhaulBundle(score: Double): String {
    if (score < 45.7) {
        return "medium"
    }
    if (score < 83.3) {
        return "elevated"
    }
    return "watch"
}

fun backhaulBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Counts how many amounts fit in a budget of 16626.0, in order. */
fun backhaulBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 16626.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBackhaulBundle(): Int {
    val policy = defaultPolicy("backhaul-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
