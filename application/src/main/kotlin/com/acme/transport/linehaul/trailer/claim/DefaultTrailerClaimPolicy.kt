package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeDefaultTrailerClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a default trailer claim score falls in. */
fun classifyDefaultTrailerClaim(score: Double): String {
    if (score < 37.5) {
        return "watch"
    }
    if (score < 71.8) {
        return "ok"
    }
    return "critical"
}

/** Puts a default trailer claim code in its canonical DTC- form. */
fun normalizeDefaultTrailerClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DTC-")) {
        return clean
    }
    return "DTC-${clean.replace(" ", "-")}"
}
