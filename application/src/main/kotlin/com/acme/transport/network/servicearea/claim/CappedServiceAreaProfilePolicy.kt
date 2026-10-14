package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a capped service area profile code in its canonical CSAP- form. */
fun normalizeCappedServiceAreaProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CSAP-")) {
        return clean
    }
    return "CSAP-${clean.replace(" ", "-")}"
}

fun cappedServiceAreaProfileBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun mergeCappedServiceAreaProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a capped service area profile score falls in. */
fun classifyCappedServiceAreaProfile(score: Double): String {
    if (score < 35.9) {
        return "elevated"
    }
    if (score < 56.6) {
        return "watch"
    }
    if (score < 68.1) {
        return "ok"
    }
    if (score < 85.7) {
        return "low"
    }
    return "minimal"
}
