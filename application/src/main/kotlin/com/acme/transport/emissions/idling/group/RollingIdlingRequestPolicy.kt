package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun rollingIdlingRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun mergeRollingIdlingRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a rolling idling request score falls in. */
fun classifyRollingIdlingRequest(score: Double): String {
    if (score < 16.4) {
        return "minimal"
    }
    if (score < 48.8) {
        return "high"
    }
    if (score < 81.1) {
        return "low"
    }
    if (score < 92.1) {
        return "elevated"
    }
    return "medium"
}

/** Puts a rolling idling request code in its canonical RIR- form. */
fun normalizeRollingIdlingRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RIR-")) {
        return clean
    }
    return "RIR-${clean.replace(" ", "-")}"
}
