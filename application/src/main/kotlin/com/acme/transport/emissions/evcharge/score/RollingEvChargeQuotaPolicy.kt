package com.acme.transport.emissions.evcharge.score

/** Names the band a rolling ev charge quota score falls in. */
fun classifyRollingEvChargeQuota(score: Double): String {
    if (score < 33.5) {
        return "ok"
    }
    if (score < 75.6) {
        return "watch"
    }
    if (score < 82.7) {
        return "minimal"
    }
    return "high"
}

/** Puts a rolling ev charge quota code in its canonical RECQ- form. */
fun normalizeRollingEvChargeQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RECQ-")) {
        return clean
    }
    return "RECQ-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 7447.0, in order. */
fun rollingEvChargeQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7447.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
