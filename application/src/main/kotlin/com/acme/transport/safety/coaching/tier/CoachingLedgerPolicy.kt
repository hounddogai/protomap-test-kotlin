package com.acme.transport.safety.coaching.tier

/** Puts a coaching ledger code in its canonical CL- form. */
fun normalizeCoachingLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CL-")) {
        return clean
    }
    return "CL-${clean.replace(" ", "-")}"
}

fun coachingLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Counts how many amounts fit in a budget of 7556.0, in order. */
fun coachingLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7556.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
