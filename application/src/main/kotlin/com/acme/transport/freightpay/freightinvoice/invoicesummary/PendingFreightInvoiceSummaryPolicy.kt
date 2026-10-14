package com.acme.transport.freightpay.freightinvoice.invoicesummary

/** Counts how many amounts fit in a budget of 5429.0, in order. */
fun pendingFreightInvoiceSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5429.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a pending freight invoice summary code in its canonical PFIS- form. */
fun normalizePendingFreightInvoiceSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PFIS-")) {
        return clean
    }
    return "PFIS-${clean.replace(" ", "-")}"
}
