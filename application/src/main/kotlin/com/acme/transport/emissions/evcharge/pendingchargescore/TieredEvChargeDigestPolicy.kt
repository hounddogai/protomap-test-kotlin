package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a tiered ev charge digest code in its canonical TECD- form. */
fun normalizeTieredEvChargeDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TECD-")) {
        return clean
    }
    return "TECD-${clean.replace(" ", "-")}"
}

fun mergeTieredEvChargeDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
