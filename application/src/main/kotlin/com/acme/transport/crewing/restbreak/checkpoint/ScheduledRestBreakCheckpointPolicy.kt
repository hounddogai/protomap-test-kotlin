package com.acme.transport.crewing.restbreak.checkpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeScheduledRestBreakCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a scheduled rest break checkpoint code in its canonical SRBC- form. */
fun normalizeScheduledRestBreakCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRBC-")) {
        return clean
    }
    return "SRBC-${clean.replace(" ", "-")}"
}
