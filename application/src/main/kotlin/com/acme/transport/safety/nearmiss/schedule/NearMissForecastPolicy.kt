package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a near miss forecast code in its canonical NMF- form. */
fun normalizeNearMissForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NMF-")) {
        return clean
    }
    return "NMF-${clean.replace(" ", "-")}"
}

fun mergeNearMissForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
