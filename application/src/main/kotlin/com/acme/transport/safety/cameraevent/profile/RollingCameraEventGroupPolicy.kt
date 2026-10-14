package com.acme.transport.safety.cameraevent.profile

/** Counts how many amounts fit in a budget of 3094.0, in order. */
fun rollingCameraEventGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 3094.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a rolling camera event group score falls in. */
fun classifyRollingCameraEventGroup(score: Double): String {
    if (score < 74.3) {
        return "elevated"
    }
    if (score < 76.6) {
        return "low"
    }
    if (score < 88.0) {
        return "ok"
    }
    return "watch"
}

fun rollingCameraEventGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
