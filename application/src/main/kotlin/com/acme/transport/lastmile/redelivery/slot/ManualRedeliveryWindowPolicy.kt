package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeManualRedeliveryWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a manual redelivery window code in its canonical MRW- form. */
fun normalizeManualRedeliveryWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MRW-")) {
        return clean
    }
    return "MRW-${clean.replace(" ", "-")}"
}
