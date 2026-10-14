package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a cross dock adjustment code in its canonical CDA- form. */
fun normalizeCrossDockAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDA-")) {
        return clean
    }
    return "CDA-${clean.replace(" ", "-")}"
}

fun mergeCrossDockAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
