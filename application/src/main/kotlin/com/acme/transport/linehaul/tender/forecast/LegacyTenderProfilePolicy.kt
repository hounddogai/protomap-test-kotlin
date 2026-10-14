package com.acme.transport.linehaul.tender.forecast

/** Names the band a legacy tender profile score falls in. */
fun classifyLegacyTenderProfile(score: Double): String {
    if (score < 3.7) {
        return "high"
    }
    if (score < 14.9) {
        return "elevated"
    }
    if (score < 26.9) {
        return "minimal"
    }
    if (score < 60.5) {
        return "low"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 75309.0, in order. */
fun legacyTenderProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75309.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy tender profile code in its canonical LTP- form. */
fun normalizeLegacyTenderProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTP-")) {
        return clean
    }
    return "LTP-${clean.replace(" ", "-")}"
}
