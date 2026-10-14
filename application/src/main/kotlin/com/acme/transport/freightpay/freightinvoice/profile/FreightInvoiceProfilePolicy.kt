package com.acme.transport.freightpay.freightinvoice.profile

/** Puts a freight invoice profile code in its canonical FIP- form. */
fun normalizeFreightInvoiceProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIP-")) {
        return clean
    }
    return "FIP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 97085.0, in order. */
fun freightInvoiceProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97085.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
