package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a tiered backhaul preference code in its canonical TBP- form. */
fun normalizeTieredBackhaulPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TBP-")) {
        return clean
    }
    return "TBP-${clean.replace(" ", "-")}"
}

fun mergeTieredBackhaulPreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
