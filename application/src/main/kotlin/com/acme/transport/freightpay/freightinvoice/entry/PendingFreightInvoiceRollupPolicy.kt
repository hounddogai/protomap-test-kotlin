package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.kit.numbers.roundTo

fun pendingFreightInvoiceRollupBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a pending freight invoice rollup code in its canonical PFIR- form. */
fun normalizePendingFreightInvoiceRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PFIR-")) {
        return clean
    }
    return "PFIR-${clean.replace(" ", "-")}"
}

/** Names the band a pending freight invoice rollup score falls in. */
fun classifyPendingFreightInvoiceRollup(score: Double): String {
    if (score < 58.8) {
        return "elevated"
    }
    if (score < 75.0) {
        return "critical"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 10395.0, in order. */
fun pendingFreightInvoiceRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10395.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
