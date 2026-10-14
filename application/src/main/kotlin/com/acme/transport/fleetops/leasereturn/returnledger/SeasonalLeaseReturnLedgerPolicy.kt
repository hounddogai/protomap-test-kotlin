package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 6607.0, in order. */
fun seasonalLeaseReturnLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6607.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a seasonal lease return ledger code in its canonical SLRL- form. */
fun normalizeSeasonalLeaseReturnLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SLRL-")) {
        return clean
    }
    return "SLRL-${clean.replace(" ", "-")}"
}

fun seasonalLeaseReturnLedgerBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
