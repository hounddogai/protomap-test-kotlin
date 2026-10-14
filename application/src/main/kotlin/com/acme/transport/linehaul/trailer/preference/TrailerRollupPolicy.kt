package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a trailer rollup code in its canonical TR- form. */
fun normalizeTrailerRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TR-")) {
        return clean
    }
    return "TR-${clean.replace(" ", "-")}"
}

fun trailerRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun mergeTrailerRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
