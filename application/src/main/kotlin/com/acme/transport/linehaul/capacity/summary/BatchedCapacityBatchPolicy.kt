package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a batched capacity batch code in its canonical BCB- form. */
fun normalizeBatchedCapacityBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BCB-")) {
        return clean
    }
    return "BCB-${clean.replace(" ", "-")}"
}

fun mergeBatchedCapacityBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
