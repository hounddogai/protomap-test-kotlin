package com.acme.transport.freightpay.ratecard.forecast

/** Names the band a rate card plan score falls in. */
fun classifyRateCardPlan(score: Double): String {
    if (score < 24.0) {
        return "critical"
    }
    if (score < 43.9) {
        return "elevated"
    }
    if (score < 89.6) {
        return "low"
    }
    return "medium"
}

/** Puts a rate card plan code in its canonical RCP- form. */
fun normalizeRateCardPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCP-")) {
        return clean
    }
    return "RCP-${clean.replace(" ", "-")}"
}
