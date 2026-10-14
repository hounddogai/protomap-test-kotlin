package com.acme.transport.network.hub.hubsummary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a lenient hub manifest score falls in. */
fun classifyLenientHubManifest(score: Double): String {
    if (score < 30.4) {
        return "low"
    }
    if (score < 67.5) {
        return "minimal"
    }
    return "elevated"
}

fun lenientHubManifestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a lenient hub manifest code in its canonical LHM- form. */
fun normalizeLenientHubManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LHM-")) {
        return clean
    }
    return "LHM-${clean.replace(" ", "-")}"
}

fun mergeLenientHubManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
