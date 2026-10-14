package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLegacySwapAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a legacy swap agreement code in its canonical LSA- form. */
fun normalizeLegacySwapAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LSA-")) {
        return clean
    }
    return "LSA-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 65743.0, in order. */
fun legacySwapAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 65743.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
