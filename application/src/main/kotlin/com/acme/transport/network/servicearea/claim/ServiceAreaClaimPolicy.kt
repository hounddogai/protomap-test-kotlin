package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a service area claim score falls in. */
fun classifyServiceAreaClaim(score: Double): String {
    if (score < 20.2) {
        return "medium"
    }
    if (score < 25.8) {
        return "low"
    }
    if (score < 26.2) {
        return "watch"
    }
    if (score < 40.7) {
        return "elevated"
    }
    return "minimal"
}

/** Puts a service area claim code in its canonical SAC- form. */
fun normalizeServiceAreaClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAC-")) {
        return clean
    }
    return "SAC-${clean.replace(" ", "-")}"
}

fun mergeServiceAreaClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun serviceAreaClaimBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
