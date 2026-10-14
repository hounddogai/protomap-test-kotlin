package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a capped dock door threshold code in its canonical CDDT- form. */
fun normalizeCappedDockDoorThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDDT-")) {
        return clean
    }
    return "CDDT-${clean.replace(" ", "-")}"
}

/** Names the band a capped dock door threshold score falls in. */
fun classifyCappedDockDoorThreshold(score: Double): String {
    if (score < 2.0) {
        return "elevated"
    }
    if (score < 60.5) {
        return "critical"
    }
    return "minimal"
}

fun cappedDockDoorThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun mergeCappedDockDoorThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
