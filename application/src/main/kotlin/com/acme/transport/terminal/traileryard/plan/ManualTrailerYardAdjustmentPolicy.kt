package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a manual trailer yard adjustment code in its canonical MTYA- form. */
fun normalizeManualTrailerYardAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MTYA-")) {
        return clean
    }
    return "MTYA-${clean.replace(" ", "-")}"
}

fun mergeManualTrailerYardAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
