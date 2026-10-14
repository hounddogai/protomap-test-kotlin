package com.acme.transport.crewing.qualification.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeFallbackQualificationSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a fallback qualification slot code in its canonical FQS- form. */
fun normalizeFallbackQualificationSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FQS-")) {
        return clean
    }
    return "FQS-${clean.replace(" ", "-")}"
}
