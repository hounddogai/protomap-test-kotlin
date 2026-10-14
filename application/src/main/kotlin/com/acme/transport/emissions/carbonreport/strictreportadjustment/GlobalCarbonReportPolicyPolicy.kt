package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a global carbon report policy score falls in. */
fun classifyGlobalCarbonReportPolicy(score: Double): String {
    if (score < 7.2) {
        return "minimal"
    }
    if (score < 24.4) {
        return "high"
    }
    if (score < 56.7) {
        return "medium"
    }
    if (score < 82.3) {
        return "critical"
    }
    return "low"
}

fun globalCarbonReportPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun mergeGlobalCarbonReportPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a global carbon report policy code in its canonical GCRP- form. */
fun normalizeGlobalCarbonReportPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCRP-")) {
        return clean
    }
    return "GCRP-${clean.replace(" ", "-")}"
}
