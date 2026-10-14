package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 77614.0, in order. */
fun lenientBrokerAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77614.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a lenient broker agreement score falls in. */
fun classifyLenientBrokerAgreement(score: Double): String {
    if (score < 36.6) {
        return "ok"
    }
    if (score < 82.2) {
        return "low"
    }
    if (score < 84.9) {
        return "medium"
    }
    return "watch"
}

fun lenientBrokerAgreementBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun lenientBrokerAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
