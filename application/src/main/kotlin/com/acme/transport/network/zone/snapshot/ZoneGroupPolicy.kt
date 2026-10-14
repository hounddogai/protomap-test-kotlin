package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a zone group score falls in. */
fun classifyZoneGroup(score: Double): String {
    if (score < 15.0) {
        return "minimal"
    }
    if (score < 72.3) {
        return "high"
    }
    if (score < 86.5) {
        return "medium"
    }
    return "low"
}

/** Puts a zone group code in its canonical ZG- form. */
fun normalizeZoneGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ZG-")) {
        return clean
    }
    return "ZG-${clean.replace(" ", "-")}"
}

fun mergeZoneGroupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
