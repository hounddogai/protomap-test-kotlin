package com.acme.transport.crewing.availability.cycle

/** Names the band a availability cycle score falls in. */
fun classifyAvailabilityCycle(score: Double): String {
    if (score < 62.8) {
        return "low"
    }
    if (score < 66.6) {
        return "elevated"
    }
    if (score < 68.0) {
        return "high"
    }
    return "critical"
}

/** Puts a availability cycle code in its canonical AC- form. */
fun normalizeAvailabilityCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AC-")) {
        return clean
    }
    return "AC-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 89872.0, in order. */
fun availabilityCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 89872.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun availabilityCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
