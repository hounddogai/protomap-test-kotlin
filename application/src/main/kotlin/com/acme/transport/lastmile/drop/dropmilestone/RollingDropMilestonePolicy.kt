package com.acme.transport.lastmile.drop.dropmilestone

/** Counts how many amounts fit in a budget of 89240.0, in order. */
fun rollingDropMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 89240.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a rolling drop milestone code in its canonical RDM- form. */
fun normalizeRollingDropMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDM-")) {
        return clean
    }
    return "RDM-${clean.replace(" ", "-")}"
}
