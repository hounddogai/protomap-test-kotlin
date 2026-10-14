package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a route offset cycle code in its canonical ROC- form. */
fun normalizeRouteOffsetCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ROC-")) {
        return clean
    }
    return "ROC-${clean.replace(" ", "-")}"
}

fun mergeRouteOffsetCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
