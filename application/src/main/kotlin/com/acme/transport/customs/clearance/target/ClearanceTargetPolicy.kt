package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeClearanceTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a clearance target code in its canonical CT- form. */
fun normalizeClearanceTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CT-")) {
        return clean
    }
    return "CT-${clean.replace(" ", "-")}"
}
