package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 82053.0, in order. */
fun trailerYardThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82053.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeTrailerYardThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun trailerYardThresholdBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a trailer yard threshold code in its canonical TYT- form. */
fun normalizeTrailerYardThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TYT-")) {
        return clean
    }
    return "TYT-${clean.replace(" ", "-")}"
}
