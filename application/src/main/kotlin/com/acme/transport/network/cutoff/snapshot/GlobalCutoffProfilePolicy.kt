package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeGlobalCutoffProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a global cutoff profile code in its canonical GCP- form. */
fun normalizeGlobalCutoffProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCP-")) {
        return clean
    }
    return "GCP-${clean.replace(" ", "-")}"
}

/** Names the band a global cutoff profile score falls in. */
fun classifyGlobalCutoffProfile(score: Double): String {
    if (score < 7.9) {
        return "minimal"
    }
    if (score < 35.5) {
        return "ok"
    }
    return "elevated"
}

fun globalCutoffProfileBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
