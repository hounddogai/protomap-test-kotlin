package com.acme.transport.crewing.availability.availabilityaudit

/** Counts how many amounts fit in a budget of 11533.0, in order. */
fun adaptiveAvailabilityAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11533.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveAvailabilityAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a adaptive availability audit code in its canonical AAA- form. */
fun normalizeAdaptiveAvailabilityAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AAA-")) {
        return clean
    }
    return "AAA-${clean.replace(" ", "-")}"
}
