package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveFreightInvoiceLedger(): Int {
    val policy = defaultPolicy("adaptive-freight-invoice-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun adaptiveFreightInvoiceLedgerBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 76076.0, in order. */
fun adaptiveFreightInvoiceLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76076.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
