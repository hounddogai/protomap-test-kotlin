package com.acme.transport.network.hub.notice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a legacy hub record code in its canonical LHR- form. */
fun normalizeLegacyHubRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LHR-")) {
        return clean
    }
    return "LHR-${clean.replace(" ", "-")}"
}

fun mergeLegacyHubRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
