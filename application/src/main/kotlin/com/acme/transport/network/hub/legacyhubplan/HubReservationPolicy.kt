package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun hubReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun mergeHubReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a hub reservation code in its canonical HR- form. */
fun normalizeHubReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HR-")) {
        return clean
    }
    return "HR-${clean.replace(" ", "-")}"
}
