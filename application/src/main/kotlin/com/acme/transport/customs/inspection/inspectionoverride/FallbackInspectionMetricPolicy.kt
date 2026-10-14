package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun fallbackInspectionMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Puts a fallback inspection metric code in its canonical FIM- form. */
fun normalizeFallbackInspectionMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIM-")) {
        return clean
    }
    return "FIM-${clean.replace(" ", "-")}"
}

fun mergeFallbackInspectionMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
