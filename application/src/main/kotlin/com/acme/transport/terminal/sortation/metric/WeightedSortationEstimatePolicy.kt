package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeWeightedSortationEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a weighted sortation estimate code in its canonical WSE- form. */
fun normalizeWeightedSortationEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WSE-")) {
        return clean
    }
    return "WSE-${clean.replace(" ", "-")}"
}
