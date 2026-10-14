package com.acme.transport.crewing.roster.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a roster digest code in its canonical RD- form. */
fun normalizeRosterDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RD-")) {
        return clean
    }
    return "RD-${clean.replace(" ", "-")}"
}

fun mergeRosterDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
