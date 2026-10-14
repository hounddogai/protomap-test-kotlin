package com.acme.transport.fleetops.utilization.segment

/** Puts a regional utilization segment code in its canonical RUS- form. */
fun normalizeRegionalUtilizationSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RUS-")) {
        return clean
    }
    return "RUS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 53698.0, in order. */
fun regionalUtilizationSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53698.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
