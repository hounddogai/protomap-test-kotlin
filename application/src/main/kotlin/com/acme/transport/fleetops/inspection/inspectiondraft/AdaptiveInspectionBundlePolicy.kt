package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a adaptive inspection bundle code in its canonical AIB- form. */
fun normalizeAdaptiveInspectionBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AIB-")) {
        return clean
    }
    return "AIB-${clean.replace(" ", "-")}"
}

/** Names the band a adaptive inspection bundle score falls in. */
fun classifyAdaptiveInspectionBundle(score: Double): String {
    if (score < 8.6) {
        return "watch"
    }
    if (score < 49.3) {
        return "critical"
    }
    return "ok"
}

fun adaptiveInspectionBundleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun mergeAdaptiveInspectionBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
