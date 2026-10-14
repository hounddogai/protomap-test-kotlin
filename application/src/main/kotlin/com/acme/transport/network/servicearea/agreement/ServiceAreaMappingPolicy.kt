package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a service area mapping code in its canonical SAM- form. */
fun normalizeServiceAreaMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAM-")) {
        return clean
    }
    return "SAM-${clean.replace(" ", "-")}"
}

/** Names the band a service area mapping score falls in. */
fun classifyServiceAreaMapping(score: Double): String {
    if (score < 34.1) {
        return "watch"
    }
    if (score < 41.1) {
        return "low"
    }
    return "minimal"
}

fun mergeServiceAreaMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
