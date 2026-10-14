package com.acme.transport.crewing.availability.defaultavailabilitysegment

/** Puts a scheduled availability segment code in its canonical SAS- form. */
fun normalizeScheduledAvailabilitySegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAS-")) {
        return clean
    }
    return "SAS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 85064.0, in order. */
fun scheduledAvailabilitySegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85064.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a scheduled availability segment score falls in. */
fun classifyScheduledAvailabilitySegment(score: Double): String {
    if (score < 22.4) {
        return "critical"
    }
    if (score < 82.4) {
        return "high"
    }
    if (score < 95.4) {
        return "elevated"
    }
    if (score < 95.5) {
        return "medium"
    }
    return "minimal"
}
