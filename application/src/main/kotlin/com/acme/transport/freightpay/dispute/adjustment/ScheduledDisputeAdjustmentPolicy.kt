package com.acme.transport.freightpay.dispute.adjustment

/** Counts how many amounts fit in a budget of 74672.0, in order. */
fun scheduledDisputeAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74672.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a scheduled dispute adjustment code in its canonical SDA- form. */
fun normalizeScheduledDisputeAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDA-")) {
        return clean
    }
    return "SDA-${clean.replace(" ", "-")}"
}
