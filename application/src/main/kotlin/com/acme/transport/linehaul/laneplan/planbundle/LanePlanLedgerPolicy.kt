package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 18866.0, in order. */
fun lanePlanLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 18866.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a lane plan ledger code in its canonical LPL- form. */
fun normalizeLanePlanLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LPL-")) {
        return clean
    }
    return "LPL-${clean.replace(" ", "-")}"
}

fun mergeLanePlanLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
