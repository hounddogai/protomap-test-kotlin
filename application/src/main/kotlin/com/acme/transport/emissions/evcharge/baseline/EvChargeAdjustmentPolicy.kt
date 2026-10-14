package com.acme.transport.emissions.evcharge.baseline

/** Counts how many amounts fit in a budget of 68643.0, in order. */
fun evChargeAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68643.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a ev charge adjustment code in its canonical ECA- form. */
fun normalizeEvChargeAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ECA-")) {
        return clean
    }
    return "ECA-${clean.replace(" ", "-")}"
}
