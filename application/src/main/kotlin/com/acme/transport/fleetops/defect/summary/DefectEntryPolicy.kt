package com.acme.transport.fleetops.defect.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun defectEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a defect entry score falls in. */
fun classifyDefectEntry(score: Double): String {
    if (score < 61.5) {
        return "critical"
    }
    if (score < 77.0) {
        return "ok"
    }
    if (score < 96.5) {
        return "high"
    }
    return "medium"
}

/** Puts a defect entry code in its canonical DE- form. */
fun normalizeDefectEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DE-")) {
        return clean
    }
    return "DE-${clean.replace(" ", "-")}"
}

fun mergeDefectEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
