package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeAdaptiveCarbonReportScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a adaptive carbon report score code in its canonical ACRS- form. */
fun normalizeAdaptiveCarbonReportScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ACRS-")) {
        return clean
    }
    return "ACRS-${clean.replace(" ", "-")}"
}
