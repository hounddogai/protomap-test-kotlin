package com.acme.transport.emissions.idling.notice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeFallbackIdlingSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a fallback idling segment code in its canonical FIS- form. */
fun normalizeFallbackIdlingSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIS-")) {
        return clean
    }
    return "FIS-${clean.replace(" ", "-")}"
}
