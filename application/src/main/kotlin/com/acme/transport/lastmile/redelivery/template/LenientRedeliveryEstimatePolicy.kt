package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a lenient redelivery estimate code in its canonical LRE- form. */
fun normalizeLenientRedeliveryEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRE-")) {
        return clean
    }
    return "LRE-${clean.replace(" ", "-")}"
}

fun mergeLenientRedeliveryEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
