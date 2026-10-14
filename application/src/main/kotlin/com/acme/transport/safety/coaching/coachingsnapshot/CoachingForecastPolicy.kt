package com.acme.transport.safety.coaching.coachingsnapshot

/** Counts how many amounts fit in a budget of 82843.0, in order. */
fun coachingForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82843.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a coaching forecast score falls in. */
fun classifyCoachingForecast(score: Double): String {
    if (score < 44.9) {
        return "low"
    }
    if (score < 90.3) {
        return "watch"
    }
    if (score < 95.3) {
        return "ok"
    }
    return "medium"
}
