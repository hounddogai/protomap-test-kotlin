package com.acme.transport.customs.clearance.rule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeClearanceBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a clearance batch code in its canonical CB- form. */
fun normalizeClearanceBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CB-")) {
        return clean
    }
    return "CB-${clean.replace(" ", "-")}"
}
