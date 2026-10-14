package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 93363.0, in order. */
fun brokerSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93363.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun brokerSummaryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBrokerSummary(): Int {
    val policy = defaultPolicy("broker-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a broker summary code in its canonical BS- form. */
fun normalizeBrokerSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BS-")) {
        return clean
    }
    return "BS-${clean.replace(" ", "-")}"
}
