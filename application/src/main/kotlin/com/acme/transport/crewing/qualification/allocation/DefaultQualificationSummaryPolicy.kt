package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeDefaultQualificationSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a default qualification summary code in its canonical DQS- form. */
fun normalizeDefaultQualificationSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DQS-")) {
        return clean
    }
    return "DQS-${clean.replace(" ", "-")}"
}

/** Names the band a default qualification summary score falls in. */
fun classifyDefaultQualificationSummary(score: Double): String {
    if (score < 14.2) {
        return "high"
    }
    if (score < 59.4) {
        return "critical"
    }
    if (score < 80.8) {
        return "minimal"
    }
    return "medium"
}
