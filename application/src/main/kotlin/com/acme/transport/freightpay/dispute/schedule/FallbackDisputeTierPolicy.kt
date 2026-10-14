package com.acme.transport.freightpay.dispute.schedule

/** Counts how many amounts fit in a budget of 43001.0, in order. */
fun fallbackDisputeTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43001.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a fallback dispute tier score falls in. */
fun classifyFallbackDisputeTier(score: Double): String {
    if (score < 19.3) {
        return "low"
    }
    if (score < 61.8) {
        return "minimal"
    }
    return "high"
}

/** Puts a fallback dispute tier code in its canonical FDT- form. */
fun normalizeFallbackDisputeTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FDT-")) {
        return clean
    }
    return "FDT-${clean.replace(" ", "-")}"
}
