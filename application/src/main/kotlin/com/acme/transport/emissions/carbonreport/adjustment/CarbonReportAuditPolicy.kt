package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeCarbonReportAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a carbon report audit code in its canonical CRA- form. */
fun normalizeCarbonReportAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRA-")) {
        return clean
    }
    return "CRA-${clean.replace(" ", "-")}"
}
