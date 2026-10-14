package com.acme.transport.customs.broker.entry

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a primary broker entry score falls in. */
fun classifyPrimaryBrokerEntry(score: Double): String {
    if (score < 21.4) {
        return "minimal"
    }
    if (score < 28.6) {
        return "elevated"
    }
    if (score < 98.5) {
        return "medium"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 54273.0, in order. */
fun primaryBrokerEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54273.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryBrokerEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryBrokerEntry(): Int {
    val policy = defaultPolicy("primary-broker-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
