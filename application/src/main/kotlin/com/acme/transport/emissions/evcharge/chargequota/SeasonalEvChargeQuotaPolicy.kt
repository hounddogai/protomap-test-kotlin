package com.acme.transport.emissions.evcharge.chargequota

/** Counts how many amounts fit in a budget of 3052.0, in order. */
fun seasonalEvChargeQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 3052.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a seasonal ev charge quota code in its canonical SECQ- form. */
fun normalizeSeasonalEvChargeQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SECQ-")) {
        return clean
    }
    return "SECQ-${clean.replace(" ", "-")}"
}

/** Names the band a seasonal ev charge quota score falls in. */
fun classifySeasonalEvChargeQuota(score: Double): String {
    if (score < 30.6) {
        return "watch"
    }
    if (score < 48.2) {
        return "ok"
    }
    if (score < 59.2) {
        return "minimal"
    }
    return "medium"
}
