package com.acme.transport.linehaul.capacity.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a capacity variance code in its canonical CV- form. */
fun normalizeCapacityVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CV-")) {
        return clean
    }
    return "CV-${clean.replace(" ", "-")}"
}

fun mergeCapacityVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
