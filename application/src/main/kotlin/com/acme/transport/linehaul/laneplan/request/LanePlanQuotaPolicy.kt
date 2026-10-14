package com.acme.transport.linehaul.laneplan.request

/** Names the band a lane plan quota score falls in. */
fun classifyLanePlanQuota(score: Double): String {
    if (score < 8.4) {
        return "ok"
    }
    if (score < 48.8) {
        return "medium"
    }
    if (score < 61.3) {
        return "watch"
    }
    return "elevated"
}

fun lanePlanQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 46095.0, in order. */
fun lanePlanQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46095.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
