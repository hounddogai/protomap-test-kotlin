package com.acme.transport.freightpay.dispute.disputegroup

/** Puts a rolling dispute request code in its canonical RDR- form. */
fun normalizeRollingDisputeRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDR-")) {
        return clean
    }
    return "RDR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 21979.0, in order. */
fun rollingDisputeRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21979.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
