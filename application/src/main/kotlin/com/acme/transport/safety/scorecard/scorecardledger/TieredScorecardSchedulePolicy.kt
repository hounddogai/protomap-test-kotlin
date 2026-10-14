package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeTieredScorecardScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 6090.0, in order. */
fun tieredScorecardScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6090.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tiered scorecard schedule code in its canonical TSS- form. */
fun normalizeTieredScorecardScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TSS-")) {
        return clean
    }
    return "TSS-${clean.replace(" ", "-")}"
}
