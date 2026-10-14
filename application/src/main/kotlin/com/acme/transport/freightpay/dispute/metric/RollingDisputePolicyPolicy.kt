package com.acme.transport.freightpay.dispute.metric

/** Counts how many amounts fit in a budget of 62098.0, in order. */
fun rollingDisputePolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62098.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a rolling dispute policy code in its canonical RDP- form. */
fun normalizeRollingDisputePolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDP-")) {
        return clean
    }
    return "RDP-${clean.replace(" ", "-")}"
}
