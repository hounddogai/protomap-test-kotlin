package com.acme.transport.crewing.restbreak.breakrollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRollingRestBreakRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a rolling rest break rollup score falls in. */
fun classifyRollingRestBreakRollup(score: Double): String {
    if (score < 1.8) {
        return "high"
    }
    if (score < 96.8) {
        return "medium"
    }
    if (score < 97.2) {
        return "watch"
    }
    return "low"
}

/** Puts a rolling rest break rollup code in its canonical RRBR- form. */
fun normalizeRollingRestBreakRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRBR-")) {
        return clean
    }
    return "RRBR-${clean.replace(" ", "-")}"
}
