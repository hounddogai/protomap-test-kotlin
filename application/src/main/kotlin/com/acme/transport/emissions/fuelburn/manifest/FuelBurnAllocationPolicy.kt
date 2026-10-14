package com.acme.transport.emissions.fuelburn.manifest

/** Counts how many amounts fit in a budget of 5216.0, in order. */
fun fuelBurnAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5216.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a fuel burn allocation code in its canonical FBA- form. */
fun normalizeFuelBurnAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FBA-")) {
        return clean
    }
    return "FBA-${clean.replace(" ", "-")}"
}
