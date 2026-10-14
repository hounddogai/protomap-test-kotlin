package com.acme.transport.customs.broker.forecast

/** Names the band a weighted broker forecast score falls in. */
fun classifyWeightedBrokerForecast(score: Double): String {
    if (score < 29.3) {
        return "medium"
    }
    if (score < 40.1) {
        return "low"
    }
    if (score < 49.4) {
        return "watch"
    }
    if (score < 88.4) {
        return "minimal"
    }
    return "elevated"
}

fun weightedBrokerForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
