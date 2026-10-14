package com.acme.transport.safety.nearmiss.missrecord

/** Puts a pending near miss milestone code in its canonical PNMM- form. */
fun normalizePendingNearMissMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PNMM-")) {
        return clean
    }
    return "PNMM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 28949.0, in order. */
fun pendingNearMissMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 28949.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
