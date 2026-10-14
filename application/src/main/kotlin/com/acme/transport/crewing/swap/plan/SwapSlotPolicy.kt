package com.acme.transport.crewing.swap.plan

/** Puts a swap slot code in its canonical SS- form. */
fun normalizeSwapSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SS-")) {
        return clean
    }
    return "SS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 90364.0, in order. */
fun swapSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90364.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
