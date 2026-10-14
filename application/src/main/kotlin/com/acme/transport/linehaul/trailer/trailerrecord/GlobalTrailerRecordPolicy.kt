package com.acme.transport.linehaul.trailer.trailerrecord

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a global trailer record score falls in. */
fun classifyGlobalTrailerRecord(score: Double): String {
    if (score < 13.8) {
        return "low"
    }
    if (score < 47.7) {
        return "minimal"
    }
    if (score < 53.4) {
        return "elevated"
    }
    if (score < 59.6) {
        return "medium"
    }
    return "watch"
}

fun mergeGlobalTrailerRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a global trailer record code in its canonical GTR- form. */
fun normalizeGlobalTrailerRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GTR-")) {
        return clean
    }
    return "GTR-${clean.replace(" ", "-")}"
}
