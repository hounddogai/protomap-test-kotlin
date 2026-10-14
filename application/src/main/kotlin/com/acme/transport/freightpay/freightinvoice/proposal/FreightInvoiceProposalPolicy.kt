package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 91903.0, in order. */
fun freightInvoiceProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 91903.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun freightInvoiceProposalBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeFreightInvoiceProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a freight invoice proposal code in its canonical FIP- form. */
fun normalizeFreightInvoiceProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIP-")) {
        return clean
    }
    return "FIP-${clean.replace(" ", "-")}"
}
