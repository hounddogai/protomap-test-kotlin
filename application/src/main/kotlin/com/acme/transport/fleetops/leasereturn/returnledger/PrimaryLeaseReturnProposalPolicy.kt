package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 86943.0, in order. */
fun primaryLeaseReturnProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86943.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryLeaseReturnProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.41
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a primary lease return proposal code in its canonical PLRP- form. */
fun normalizePrimaryLeaseReturnProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PLRP-")) {
        return clean
    }
    return "PLRP-${clean.replace(" ", "-")}"
}
