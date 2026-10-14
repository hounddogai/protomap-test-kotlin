package com.acme.transport.network.node.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLenientNodeBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a lenient node batch code in its canonical LNB- form. */
fun normalizeLenientNodeBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LNB-")) {
        return clean
    }
    return "LNB-${clean.replace(" ", "-")}"
}
