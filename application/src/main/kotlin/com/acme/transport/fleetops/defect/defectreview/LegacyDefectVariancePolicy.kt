package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLegacyDefectVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a legacy defect variance score falls in. */
fun classifyLegacyDefectVariance(score: Double): String {
    if (score < 70.2) {
        return "minimal"
    }
    if (score < 81.3) {
        return "high"
    }
    return "elevated"
}

/** Puts a legacy defect variance code in its canonical LDV- form. */
fun normalizeLegacyDefectVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDV-")) {
        return clean
    }
    return "LDV-${clean.replace(" ", "-")}"
}
