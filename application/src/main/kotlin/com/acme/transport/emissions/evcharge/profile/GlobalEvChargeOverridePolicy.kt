package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a global ev charge override code in its canonical GECO- form. */
fun normalizeGlobalEvChargeOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GECO-")) {
        return clean
    }
    return "GECO-${clean.replace(" ", "-")}"
}

fun mergeGlobalEvChargeOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
