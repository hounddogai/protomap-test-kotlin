package com.acme.transport.network.transittime.forecast

/** Names the band a transit time forecast score falls in. */
fun classifyTransitTimeForecast(score: Double): String {
    if (score < 24.0) {
        return "high"
    }
    if (score < 65.6) {
        return "medium"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 73790.0, in order. */
fun transitTimeForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 73790.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun transitTimeForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
