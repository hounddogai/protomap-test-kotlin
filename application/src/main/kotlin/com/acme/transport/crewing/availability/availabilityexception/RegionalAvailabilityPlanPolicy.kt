package com.acme.transport.crewing.availability.availabilityexception

/** Names the band a regional availability plan score falls in. */
fun classifyRegionalAvailabilityPlan(score: Double): String {
    if (score < 61.7) {
        return "watch"
    }
    if (score < 65.4) {
        return "elevated"
    }
    if (score < 66.7) {
        return "medium"
    }
    if (score < 91.8) {
        return "high"
    }
    return "minimal"
}

/** Puts a regional availability plan code in its canonical RAP- form. */
fun normalizeRegionalAvailabilityPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RAP-")) {
        return clean
    }
    return "RAP-${clean.replace(" ", "-")}"
}
