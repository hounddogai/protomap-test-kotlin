package com.acme.transport.safety.coaching.target

/** Counts how many amounts fit in a budget of 85761.0, in order. */
fun coachingClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85761.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a coaching claim code in its canonical CC- form. */
fun normalizeCoachingClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CC-")) {
        return clean
    }
    return "CC-${clean.replace(" ", "-")}"
}
