package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a batched roster adjustment code in its canonical BRA- form. */
fun normalizeBatchedRosterAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BRA-")) {
        return clean
    }
    return "BRA-${clean.replace(" ", "-")}"
}

fun mergeBatchedRosterAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 4275.0, in order. */
fun batchedRosterAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4275.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
