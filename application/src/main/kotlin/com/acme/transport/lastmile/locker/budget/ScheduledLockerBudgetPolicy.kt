package com.acme.transport.lastmile.locker.budget

/** Puts a scheduled locker budget code in its canonical SLB- form. */
fun normalizeScheduledLockerBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SLB-")) {
        return clean
    }
    return "SLB-${clean.replace(" ", "-")}"
}

fun scheduledLockerBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 37722.0, in order. */
fun scheduledLockerBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37722.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
