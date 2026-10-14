package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a rolling rest break summary code in its canonical RRBS- form. */
fun normalizeRollingRestBreakSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRBS-")) {
        return clean
    }
    return "RRBS-${clean.replace(" ", "-")}"
}

fun mergeRollingRestBreakSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 70667.0, in order. */
fun rollingRestBreakSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70667.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
