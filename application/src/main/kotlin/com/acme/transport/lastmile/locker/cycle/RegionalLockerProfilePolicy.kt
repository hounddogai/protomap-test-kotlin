package com.acme.transport.lastmile.locker.cycle

/** Counts how many amounts fit in a budget of 50579.0, in order. */
fun regionalLockerProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 50579.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a regional locker profile code in its canonical RLP- form. */
fun normalizeRegionalLockerProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RLP-")) {
        return clean
    }
    return "RLP-${clean.replace(" ", "-")}"
}
