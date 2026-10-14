package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a sortation tier code in its canonical ST- form. */
fun normalizeSortationTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ST-")) {
        return clean
    }
    return "ST-${clean.replace(" ", "-")}"
}

fun mergeSortationTierLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a sortation tier score falls in. */
fun classifySortationTier(score: Double): String {
    if (score < 47.0) {
        return "minimal"
    }
    if (score < 84.7) {
        return "high"
    }
    return "watch"
}
