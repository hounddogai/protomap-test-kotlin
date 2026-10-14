package com.acme.transport.freightpay.dispute.disputepolicy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a capped dispute policy code in its canonical CDP- form. */
fun normalizeCappedDisputePolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDP-")) {
        return clean
    }
    return "CDP-${clean.replace(" ", "-")}"
}

fun mergeCappedDisputePolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
