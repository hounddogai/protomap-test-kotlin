package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 9792.0, in order. */
fun cappedCapacityForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9792.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a capped capacity forecast code in its canonical CCF- form. */
fun normalizeCappedCapacityForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CCF-")) {
        return clean
    }
    return "CCF-${clean.replace(" ", "-")}"
}

/** Names the band a capped capacity forecast score falls in. */
fun classifyCappedCapacityForecast(score: Double): String {
    if (score < 50.2) {
        return "minimal"
    }
    if (score < 59.1) {
        return "low"
    }
    if (score < 84.5) {
        return "watch"
    }
    return "high"
}

fun cappedCapacityForecastBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
