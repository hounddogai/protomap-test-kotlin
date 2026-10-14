package com.acme.transport.customs.broker.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeManualBrokerDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a manual broker digest code in its canonical MBD- form. */
fun normalizeManualBrokerDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MBD-")) {
        return clean
    }
    return "MBD-${clean.replace(" ", "-")}"
}
