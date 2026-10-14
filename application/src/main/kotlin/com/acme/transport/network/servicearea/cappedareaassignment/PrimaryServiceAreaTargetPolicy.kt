package com.acme.transport.network.servicearea.cappedareaassignment

fun primaryServiceAreaTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a primary service area target code in its canonical PSAT- form. */
fun normalizePrimaryServiceAreaTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PSAT-")) {
        return clean
    }
    return "PSAT-${clean.replace(" ", "-")}"
}

/** Names the band a primary service area target score falls in. */
fun classifyPrimaryServiceAreaTarget(score: Double): String {
    if (score < 4.1) {
        return "low"
    }
    if (score < 89.0) {
        return "critical"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 77782.0, in order. */
fun primaryServiceAreaTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77782.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
