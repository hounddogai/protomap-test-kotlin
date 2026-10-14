package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.kit.numbers.roundTo

/** Puts a freight invoice metric code in its canonical FIM- form. */
fun normalizeFreightInvoiceMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIM-")) {
        return clean
    }
    return "FIM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 41478.0, in order. */
fun freightInvoiceMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 41478.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun freightInvoiceMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun freightInvoiceMetricBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
