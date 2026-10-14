package com.acme.transport.terminal.crossdock.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a cross dock target code in its canonical CDT- form. */
fun normalizeCrossDockTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDT-")) {
        return clean
    }
    return "CDT-${clean.replace(" ", "-")}"
}

fun mergeCrossDockTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a cross dock target score falls in. */
fun classifyCrossDockTarget(score: Double): String {
    if (score < 49.1) {
        return "high"
    }
    if (score < 59.8) {
        return "minimal"
    }
    if (score < 85.1) {
        return "critical"
    }
    return "low"
}
