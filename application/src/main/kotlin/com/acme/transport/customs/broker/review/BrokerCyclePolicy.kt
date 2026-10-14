package com.acme.transport.customs.broker.review

/** Names the band a broker cycle score falls in. */
fun classifyBrokerCycle(score: Double): String {
    if (score < 2.4) {
        return "high"
    }
    if (score < 86.3) {
        return "medium"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 43273.0, in order. */
fun brokerCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43273.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
