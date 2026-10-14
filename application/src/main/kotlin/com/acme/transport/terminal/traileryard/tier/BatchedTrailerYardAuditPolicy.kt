package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a batched trailer yard audit code in its canonical BTYA- form. */
fun normalizeBatchedTrailerYardAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BTYA-")) {
        return clean
    }
    return "BTYA-${clean.replace(" ", "-")}"
}

fun mergeBatchedTrailerYardAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
