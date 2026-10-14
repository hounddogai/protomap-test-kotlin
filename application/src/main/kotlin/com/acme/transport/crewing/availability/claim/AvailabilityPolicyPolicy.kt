package com.acme.transport.crewing.availability.claim

/** Puts a availability policy code in its canonical AP- form. */
fun normalizeAvailabilityPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AP-")) {
        return clean
    }
    return "AP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 57833.0, in order. */
fun availabilityPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 57833.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
