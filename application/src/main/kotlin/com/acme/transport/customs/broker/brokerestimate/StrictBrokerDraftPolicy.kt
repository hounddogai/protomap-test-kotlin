package com.acme.transport.customs.broker.brokerestimate

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 42782.0, in order. */
fun strictBrokerDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 42782.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictBrokerDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictBrokerDraft(): Int {
    val policy = defaultPolicy("strict-broker-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
