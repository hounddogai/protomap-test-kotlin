package com.acme.transport.terminal.traileryard.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a scheduled trailer yard claim code in its canonical STYC- form. */
fun normalizeScheduledTrailerYardClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STYC-")) {
        return clean
    }
    return "STYC-${clean.replace(" ", "-")}"
}

fun mergeScheduledTrailerYardClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
