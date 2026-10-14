package com.acme.transport.safety.violation.baseline

/** Puts a global violation tier code in its canonical GVT- form. */
fun normalizeGlobalViolationTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GVT-")) {
        return clean
    }
    return "GVT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 22205.0, in order. */
fun globalViolationTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22205.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
