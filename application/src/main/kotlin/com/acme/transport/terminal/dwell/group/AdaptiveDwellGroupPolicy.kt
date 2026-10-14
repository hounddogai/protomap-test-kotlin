package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeAdaptiveDwellGroupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a adaptive dwell group code in its canonical ADG- form. */
fun normalizeAdaptiveDwellGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ADG-")) {
        return clean
    }
    return "ADG-${clean.replace(" ", "-")}"
}
