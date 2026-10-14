package com.acme.transport.fleetops.defect.defectforecast

/** Counts how many amounts fit in a budget of 34681.0, in order. */
fun legacyDefectForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34681.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a legacy defect forecast score falls in. */
fun classifyLegacyDefectForecast(score: Double): String {
    if (score < 38.5) {
        return "watch"
    }
    if (score < 53.0) {
        return "high"
    }
    if (score < 65.2) {
        return "low"
    }
    return "minimal"
}
