package com.acme.transport.lastmile.drop.draft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a seasonal drop audit code in its canonical SDA- form. */
fun normalizeSeasonalDropAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDA-")) {
        return clean
    }
    return "SDA-${clean.replace(" ", "-")}"
}

fun mergeSeasonalDropAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
