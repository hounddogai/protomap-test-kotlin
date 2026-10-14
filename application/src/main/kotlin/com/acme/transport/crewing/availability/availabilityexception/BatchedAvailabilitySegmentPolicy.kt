package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeBatchedAvailabilitySegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a batched availability segment code in its canonical BAS- form. */
fun normalizeBatchedAvailabilitySegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BAS-")) {
        return clean
    }
    return "BAS-${clean.replace(" ", "-")}"
}
