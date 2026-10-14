package com.acme.transport.crewing.availability.availabilitysegment

/** Names the band a default availability agreement score falls in. */
fun classifyDefaultAvailabilityAgreement(score: Double): String {
    if (score < 14.6) {
        return "minimal"
    }
    if (score < 47.4) {
        return "medium"
    }
    if (score < 56.4) {
        return "critical"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 16583.0, in order. */
fun defaultAvailabilityAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 16583.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun defaultAvailabilityAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a default availability agreement code in its canonical DAA- form. */
fun normalizeDefaultAvailabilityAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DAA-")) {
        return clean
    }
    return "DAA-${clean.replace(" ", "-")}"
}
