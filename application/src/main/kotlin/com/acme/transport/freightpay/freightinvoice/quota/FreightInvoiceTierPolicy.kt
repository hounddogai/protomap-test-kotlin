package com.acme.transport.freightpay.freightinvoice.quota

/** Puts a freight invoice tier code in its canonical FIT- form. */
fun normalizeFreightInvoiceTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIT-")) {
        return clean
    }
    return "FIT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 54878.0, in order. */
fun freightInvoiceTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54878.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
