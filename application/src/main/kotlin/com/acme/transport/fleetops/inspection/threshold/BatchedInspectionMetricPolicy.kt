package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeBatchedInspectionMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a batched inspection metric code in its canonical BIM- form. */
fun normalizeBatchedInspectionMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BIM-")) {
        return clean
    }
    return "BIM-${clean.replace(" ", "-")}"
}
