package com.acme.transport.terminal.traileryard.score

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeTrailerYardScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a trailer yard score code in its canonical TYS- form. */
fun normalizeTrailerYardScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TYS-")) {
        return clean
    }
    return "TYS-${clean.replace(" ", "-")}"
}

fun trailerYardScoreBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
