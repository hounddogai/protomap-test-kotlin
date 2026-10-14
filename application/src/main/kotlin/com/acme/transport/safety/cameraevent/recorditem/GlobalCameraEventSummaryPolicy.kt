package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a global camera event summary score falls in. */
fun classifyGlobalCameraEventSummary(score: Double): String {
    if (score < 11.0) {
        return "minimal"
    }
    if (score < 21.1) {
        return "elevated"
    }
    if (score < 86.5) {
        return "low"
    }
    return "high"
}

/** Puts a global camera event summary code in its canonical GCES- form. */
fun normalizeGlobalCameraEventSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCES-")) {
        return clean
    }
    return "GCES-${clean.replace(" ", "-")}"
}

fun globalCameraEventSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun mergeGlobalCameraEventSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
