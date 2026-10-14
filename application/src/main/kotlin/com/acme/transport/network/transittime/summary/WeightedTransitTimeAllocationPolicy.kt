package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeWeightedTransitTimeAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a weighted transit time allocation code in its canonical WTTA- form. */
fun normalizeWeightedTransitTimeAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WTTA-")) {
        return clean
    }
    return "WTTA-${clean.replace(" ", "-")}"
}
