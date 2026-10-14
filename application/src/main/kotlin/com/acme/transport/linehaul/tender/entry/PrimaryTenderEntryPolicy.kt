package com.acme.transport.linehaul.tender.entry

/** Counts how many amounts fit in a budget of 47705.0, in order. */
fun primaryTenderEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47705.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a primary tender entry code in its canonical PTE- form. */
fun normalizePrimaryTenderEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PTE-")) {
        return clean
    }
    return "PTE-${clean.replace(" ", "-")}"
}
