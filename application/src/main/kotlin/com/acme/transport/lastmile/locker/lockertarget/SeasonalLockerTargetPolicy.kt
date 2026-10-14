package com.acme.transport.lastmile.locker.lockertarget

/** Counts how many amounts fit in a budget of 83407.0, in order. */
fun seasonalLockerTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83407.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a seasonal locker target code in its canonical SLT- form. */
fun normalizeSeasonalLockerTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SLT-")) {
        return clean
    }
    return "SLT-${clean.replace(" ", "-")}"
}
