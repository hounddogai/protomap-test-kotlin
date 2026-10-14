package com.acme.transport.linehaul.trailer.segment

/** Counts how many amounts fit in a budget of 31366.0, in order. */
fun manualTrailerOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 31366.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a manual trailer override code in its canonical MTO- form. */
fun normalizeManualTrailerOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MTO-")) {
        return clean
    }
    return "MTO-${clean.replace(" ", "-")}"
}

/** Names the band a manual trailer override score falls in. */
fun classifyManualTrailerOverride(score: Double): String {
    if (score < 14.6) {
        return "ok"
    }
    if (score < 37.8) {
        return "low"
    }
    if (score < 56.7) {
        return "medium"
    }
    return "minimal"
}
