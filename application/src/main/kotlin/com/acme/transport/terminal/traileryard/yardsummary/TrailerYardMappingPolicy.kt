package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a trailer yard mapping code in its canonical TYM- form. */
fun normalizeTrailerYardMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TYM-")) {
        return clean
    }
    return "TYM-${clean.replace(" ", "-")}"
}

fun mergeTrailerYardMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 40045.0, in order. */
fun trailerYardMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 40045.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun trailerYardMappingBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
