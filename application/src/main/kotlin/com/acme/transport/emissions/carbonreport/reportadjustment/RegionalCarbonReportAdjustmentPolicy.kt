package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun regionalCarbonReportAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun mergeRegionalCarbonReportAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a regional carbon report adjustment code in its canonical RCRA- form. */
fun normalizeRegionalCarbonReportAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCRA-")) {
        return clean
    }
    return "RCRA-${clean.replace(" ", "-")}"
}
