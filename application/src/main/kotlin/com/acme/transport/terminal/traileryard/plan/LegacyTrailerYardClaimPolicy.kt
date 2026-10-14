package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a legacy trailer yard claim code in its canonical LTYC- form. */
fun normalizeLegacyTrailerYardClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTYC-")) {
        return clean
    }
    return "LTYC-${clean.replace(" ", "-")}"
}

fun legacyTrailerYardClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun mergeLegacyTrailerYardClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
