package com.acme.transport.network.node.forecast

/** Counts how many amounts fit in a budget of 81947.0, in order. */
fun nodeForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 81947.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a node forecast score falls in. */
fun classifyNodeForecast(score: Double): String {
    if (score < 2.0) {
        return "elevated"
    }
    if (score < 97.4) {
        return "medium"
    }
    return "minimal"
}
