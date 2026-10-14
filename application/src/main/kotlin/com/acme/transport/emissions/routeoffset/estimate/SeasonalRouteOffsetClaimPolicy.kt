package com.acme.transport.emissions.routeoffset.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeSeasonalRouteOffsetClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a seasonal route offset claim code in its canonical SROC- form. */
fun normalizeSeasonalRouteOffsetClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SROC-")) {
        return clean
    }
    return "SROC-${clean.replace(" ", "-")}"
}
