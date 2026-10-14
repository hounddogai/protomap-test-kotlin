package com.acme.transport.customs.broker.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a default broker digest code in its canonical DBD- form. */
fun normalizeDefaultBrokerDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DBD-")) {
        return clean
    }
    return "DBD-${clean.replace(" ", "-")}"
}

fun mergeDefaultBrokerDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
