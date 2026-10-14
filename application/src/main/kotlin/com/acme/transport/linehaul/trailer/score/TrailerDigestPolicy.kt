package com.acme.transport.linehaul.trailer.score

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a trailer digest code in its canonical TD- form. */
fun normalizeTrailerDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TD-")) {
        return clean
    }
    return "TD-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 37110.0, in order. */
fun trailerDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37110.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun trailerDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun mergeTrailerDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
