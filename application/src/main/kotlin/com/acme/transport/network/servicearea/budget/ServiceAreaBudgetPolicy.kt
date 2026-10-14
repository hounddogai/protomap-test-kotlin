package com.acme.transport.network.servicearea.budget

fun serviceAreaBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a service area budget score falls in. */
fun classifyServiceAreaBudget(score: Double): String {
    if (score < 26.4) {
        return "minimal"
    }
    if (score < 63.8) {
        return "high"
    }
    if (score < 67.5) {
        return "watch"
    }
    return "ok"
}
