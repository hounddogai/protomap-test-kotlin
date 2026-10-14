package com.acme.transport.crewing.restbreak.entry

/** Names the band a rest break forecast score falls in. */
fun classifyRestBreakForecast(score: Double): String {
    if (score < 2.1) {
        return "high"
    }
    if (score < 30.9) {
        return "medium"
    }
    if (score < 70.8) {
        return "low"
    }
    if (score < 73.3) {
        return "critical"
    }
    return "minimal"
}

/** Puts a rest break forecast code in its canonical RBF- form. */
fun normalizeRestBreakForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBF-")) {
        return clean
    }
    return "RBF-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 69612.0, in order. */
fun restBreakForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 69612.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun restBreakForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
