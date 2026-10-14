package com.acme.transport.customs.hold.quota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeHoldQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a hold quota code in its canonical HQ- form. */
fun normalizeHoldQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HQ-")) {
        return clean
    }
    return "HQ-${clean.replace(" ", "-")}"
}
