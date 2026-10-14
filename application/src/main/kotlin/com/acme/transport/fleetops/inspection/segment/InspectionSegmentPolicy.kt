package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a inspection segment score falls in. */
fun classifyInspectionSegment(score: Double): String {
    if (score < 43.9) {
        return "critical"
    }
    if (score < 52.9) {
        return "ok"
    }
    if (score < 76.1) {
        return "medium"
    }
    if (score < 97.2) {
        return "low"
    }
    return "minimal"
}

/** Puts a inspection segment code in its canonical IS- form. */
fun normalizeInspectionSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IS-")) {
        return clean
    }
    return "IS-${clean.replace(" ", "-")}"
}

fun mergeInspectionSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun inspectionSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
