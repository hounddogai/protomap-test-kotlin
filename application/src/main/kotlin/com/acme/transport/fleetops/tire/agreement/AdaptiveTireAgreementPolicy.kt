package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.numbers.roundTo

fun adaptiveTireAgreementBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a adaptive tire agreement code in its canonical ATA- form. */
fun normalizeAdaptiveTireAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ATA-")) {
        return clean
    }
    return "ATA-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 95411.0, in order. */
fun adaptiveTireAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 95411.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
