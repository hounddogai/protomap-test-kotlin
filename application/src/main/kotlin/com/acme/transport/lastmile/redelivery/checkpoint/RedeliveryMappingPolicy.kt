package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a redelivery mapping code in its canonical RM- form. */
fun normalizeRedeliveryMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RM-")) {
        return clean
    }
    return "RM-${clean.replace(" ", "-")}"
}

fun mergeRedeliveryMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
