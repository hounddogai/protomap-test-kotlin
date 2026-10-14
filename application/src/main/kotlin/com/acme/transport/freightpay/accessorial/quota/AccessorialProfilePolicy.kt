package com.acme.transport.freightpay.accessorial.quota

/** Puts a accessorial profile code in its canonical AP- form. */
fun normalizeAccessorialProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AP-")) {
        return clean
    }
    return "AP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 36674.0, in order. */
fun accessorialProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 36674.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
