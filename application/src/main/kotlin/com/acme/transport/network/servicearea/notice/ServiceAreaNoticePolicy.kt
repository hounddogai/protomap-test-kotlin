package com.acme.transport.network.servicearea.notice

/** Counts how many amounts fit in a budget of 96942.0, in order. */
fun serviceAreaNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 96942.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a service area notice score falls in. */
fun classifyServiceAreaNotice(score: Double): String {
    if (score < 33.2) {
        return "minimal"
    }
    if (score < 56.8) {
        return "critical"
    }
    if (score < 96.5) {
        return "low"
    }
    return "watch"
}
