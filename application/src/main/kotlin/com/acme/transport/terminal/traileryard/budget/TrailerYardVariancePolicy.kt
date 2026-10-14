package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeTrailerYardVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a trailer yard variance code in its canonical TYV- form. */
fun normalizeTrailerYardVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TYV-")) {
        return clean
    }
    return "TYV-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 38944.0, in order. */
fun trailerYardVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38944.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
