package com.acme.transport.network.servicearea.notice

/** Names the band a service area profile score falls in. */
fun classifyServiceAreaProfile(score: Double): String {
    if (score < 61.8) {
        return "elevated"
    }
    if (score < 71.7) {
        return "medium"
    }
    if (score < 99.3) {
        return "minimal"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 68592.0, in order. */
fun serviceAreaProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68592.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun serviceAreaProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
