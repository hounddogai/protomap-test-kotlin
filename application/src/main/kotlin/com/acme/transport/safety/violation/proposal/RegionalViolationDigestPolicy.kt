package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRegionalViolationDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a regional violation digest code in its canonical RVD- form. */
fun normalizeRegionalViolationDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RVD-")) {
        return clean
    }
    return "RVD-${clean.replace(" ", "-")}"
}

/** Names the band a regional violation digest score falls in. */
fun classifyRegionalViolationDigest(score: Double): String {
    if (score < 62.0) {
        return "minimal"
    }
    if (score < 79.6) {
        return "low"
    }
    return "elevated"
}
