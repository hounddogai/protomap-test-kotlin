package com.acme.transport.crewing.availability.claim

/** Names the band a availability claim score falls in. */
fun classifyAvailabilityClaim(score: Double): String {
    if (score < 8.1) {
        return "medium"
    }
    if (score < 31.2) {
        return "high"
    }
    if (score < 83.2) {
        return "low"
    }
    if (score < 99.6) {
        return "elevated"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 71413.0, in order. */
fun availabilityClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71413.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a availability claim code in its canonical AC- form. */
fun normalizeAvailabilityClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AC-")) {
        return clean
    }
    return "AC-${clean.replace(" ", "-")}"
}
