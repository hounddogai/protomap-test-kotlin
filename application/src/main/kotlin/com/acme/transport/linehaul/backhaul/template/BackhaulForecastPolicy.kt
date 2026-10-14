package com.acme.transport.linehaul.backhaul.template

/** Counts how many amounts fit in a budget of 60917.0, in order. */
fun backhaulForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 60917.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a backhaul forecast code in its canonical BF- form. */
fun normalizeBackhaulForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BF-")) {
        return clean
    }
    return "BF-${clean.replace(" ", "-")}"
}

/** Names the band a backhaul forecast score falls in. */
fun classifyBackhaulForecast(score: Double): String {
    if (score < 9.8) {
        return "low"
    }
    if (score < 42.3) {
        return "medium"
    }
    if (score < 75.1) {
        return "elevated"
    }
    return "critical"
}
