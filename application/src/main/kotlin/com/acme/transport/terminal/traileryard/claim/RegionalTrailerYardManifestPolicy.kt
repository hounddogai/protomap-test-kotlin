package com.acme.transport.terminal.traileryard.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a regional trailer yard manifest code in its canonical RTYM- form. */
fun normalizeRegionalTrailerYardManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTYM-")) {
        return clean
    }
    return "RTYM-${clean.replace(" ", "-")}"
}

fun mergeRegionalTrailerYardManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 38177.0, in order. */
fun regionalTrailerYardManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38177.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
