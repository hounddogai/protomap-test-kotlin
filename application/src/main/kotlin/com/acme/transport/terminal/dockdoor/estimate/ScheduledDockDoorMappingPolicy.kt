package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a scheduled dock door mapping code in its canonical SDDM- form. */
fun normalizeScheduledDockDoorMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDDM-")) {
        return clean
    }
    return "SDDM-${clean.replace(" ", "-")}"
}

fun mergeScheduledDockDoorMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
