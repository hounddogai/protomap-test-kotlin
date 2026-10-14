package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergePendingRouteOffsetDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a pending route offset digest code in its canonical PROD- form. */
fun normalizePendingRouteOffsetDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PROD-")) {
        return clean
    }
    return "PROD-${clean.replace(" ", "-")}"
}
