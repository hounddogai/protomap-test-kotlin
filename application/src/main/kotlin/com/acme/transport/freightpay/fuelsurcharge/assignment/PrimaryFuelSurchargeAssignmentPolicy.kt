package com.acme.transport.freightpay.fuelsurcharge.assignment

/** Counts how many amounts fit in a budget of 63283.0, in order. */
fun primaryFuelSurchargeAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63283.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a primary fuel surcharge assignment code in its canonical PFSA- form. */
fun normalizePrimaryFuelSurchargeAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PFSA-")) {
        return clean
    }
    return "PFSA-${clean.replace(" ", "-")}"
}
