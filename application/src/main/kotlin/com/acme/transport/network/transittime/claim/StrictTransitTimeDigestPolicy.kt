package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun strictTransitTimeDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Puts a strict transit time digest code in its canonical STTD- form. */
fun normalizeStrictTransitTimeDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STTD-")) {
        return clean
    }
    return "STTD-${clean.replace(" ", "-")}"
}

fun mergeStrictTransitTimeDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
