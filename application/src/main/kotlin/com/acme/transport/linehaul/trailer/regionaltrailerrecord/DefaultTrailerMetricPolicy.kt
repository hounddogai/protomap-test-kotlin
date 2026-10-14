package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeDefaultTrailerMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a default trailer metric code in its canonical DTM- form. */
fun normalizeDefaultTrailerMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DTM-")) {
        return clean
    }
    return "DTM-${clean.replace(" ", "-")}"
}
