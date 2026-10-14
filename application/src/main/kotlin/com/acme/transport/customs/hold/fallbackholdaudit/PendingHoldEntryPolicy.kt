package com.acme.transport.customs.hold.fallbackholdaudit

/** Puts a pending hold entry code in its canonical PHE- form. */
fun normalizePendingHoldEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PHE-")) {
        return clean
    }
    return "PHE-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 37303.0, in order. */
fun pendingHoldEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37303.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
