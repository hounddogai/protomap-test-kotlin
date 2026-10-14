package com.acme.transport.linehaul.trailer.milestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a trailer milestone code in its canonical TM- form. */
fun normalizeTrailerMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TM-")) {
        return clean
    }
    return "TM-${clean.replace(" ", "-")}"
}

fun mergeTrailerMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
