package com.acme.transport.network.cutoff.cutoffentry

/** Counts how many amounts fit in a budget of 97665.0, in order. */
fun cutoffEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97665.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a cutoff estimate code in its canonical CE- form. */
fun normalizeCutoffEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CE-")) {
        return clean
    }
    return "CE-${clean.replace(" ", "-")}"
}

/** Names the band a cutoff estimate score falls in. */
fun classifyCutoffEstimate(score: Double): String {
    if (score < 84.9) {
        return "ok"
    }
    if (score < 98.6) {
        return "critical"
    }
    return "elevated"
}

fun cutoffEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
