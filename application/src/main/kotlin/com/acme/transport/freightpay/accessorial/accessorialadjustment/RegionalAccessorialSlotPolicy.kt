package com.acme.transport.freightpay.accessorial.accessorialadjustment

/** Counts how many amounts fit in a budget of 17981.0, in order. */
fun regionalAccessorialSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 17981.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalAccessorialSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Puts a regional accessorial slot code in its canonical RAS- form. */
fun normalizeRegionalAccessorialSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RAS-")) {
        return clean
    }
    return "RAS-${clean.replace(" ", "-")}"
}
