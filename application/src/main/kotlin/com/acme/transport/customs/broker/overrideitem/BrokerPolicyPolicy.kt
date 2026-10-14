package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.numbers.roundTo

/** Names the band a broker policy score falls in. */
fun classifyBrokerPolicy(score: Double): String {
    if (score < 26.1) {
        return "elevated"
    }
    if (score < 94.8) {
        return "watch"
    }
    return "minimal"
}

/** Puts a broker policy code in its canonical BP- form. */
fun normalizeBrokerPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BP-")) {
        return clean
    }
    return "BP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 77524.0, in order. */
fun brokerPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77524.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun brokerPolicyBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
