package com.acme.transport.lastmile.locker.preference

/** Puts a locker preference code in its canonical LP- form. */
fun normalizeLockerPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LP-")) {
        return clean
    }
    return "LP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 41083.0, in order. */
fun lockerPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 41083.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
