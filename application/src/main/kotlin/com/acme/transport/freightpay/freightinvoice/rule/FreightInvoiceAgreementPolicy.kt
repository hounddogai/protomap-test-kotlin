package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.kit.numbers.roundTo

fun freightInvoiceAgreementBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 79962.0, in order. */
fun freightInvoiceAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79962.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
