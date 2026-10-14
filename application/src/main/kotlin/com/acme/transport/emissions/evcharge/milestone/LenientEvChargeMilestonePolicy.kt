package com.acme.transport.emissions.evcharge.milestone

/** Counts how many amounts fit in a budget of 87622.0, in order. */
fun lenientEvChargeMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 87622.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a lenient ev charge milestone code in its canonical LECM- form. */
fun normalizeLenientEvChargeMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LECM-")) {
        return clean
    }
    return "LECM-${clean.replace(" ", "-")}"
}
