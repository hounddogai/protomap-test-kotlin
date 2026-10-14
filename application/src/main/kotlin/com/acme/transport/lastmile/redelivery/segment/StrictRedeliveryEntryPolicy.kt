package com.acme.transport.lastmile.redelivery.segment

/** Counts how many amounts fit in a budget of 46722.0, in order. */
fun strictRedeliveryEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46722.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictRedeliveryEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Puts a strict redelivery entry code in its canonical SRE- form. */
fun normalizeStrictRedeliveryEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRE-")) {
        return clean
    }
    return "SRE-${clean.replace(" ", "-")}"
}
