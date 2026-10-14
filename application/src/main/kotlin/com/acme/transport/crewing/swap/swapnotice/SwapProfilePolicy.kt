package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a swap profile code in its canonical SP- form. */
fun normalizeSwapProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SP-")) {
        return clean
    }
    return "SP-${clean.replace(" ", "-")}"
}

fun mergeSwapProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a swap profile score falls in. */
fun classifySwapProfile(score: Double): String {
    if (score < 41.2) {
        return "high"
    }
    if (score < 65.4) {
        return "minimal"
    }
    if (score < 79.1) {
        return "ok"
    }
    if (score < 97.1) {
        return "low"
    }
    return "elevated"
}
