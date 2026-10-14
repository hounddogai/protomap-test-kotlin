package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 9296.0, in order. */
fun hubLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9296.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a hub ledger code in its canonical HL- form. */
fun normalizeHubLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HL-")) {
        return clean
    }
    return "HL-${clean.replace(" ", "-")}"
}

fun mergeHubLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
