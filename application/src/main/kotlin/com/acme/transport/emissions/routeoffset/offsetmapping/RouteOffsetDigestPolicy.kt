package com.acme.transport.emissions.routeoffset.offsetmapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeRouteOffsetDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a route offset digest code in its canonical ROD- form. */
fun normalizeRouteOffsetDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ROD-")) {
        return clean
    }
    return "ROD-${clean.replace(" ", "-")}"
}

/** Names the band a route offset digest score falls in. */
fun classifyRouteOffsetDigest(score: Double): String {
    if (score < 42.3) {
        return "ok"
    }
    if (score < 69.2) {
        return "watch"
    }
    if (score < 76.2) {
        return "medium"
    }
    if (score < 87.1) {
        return "minimal"
    }
    return "elevated"
}

fun routeOffsetDigestBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
